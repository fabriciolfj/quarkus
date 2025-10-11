# Stack de Observabilidade Completa com Quarkus

Este projeto configura uma stack completa de observabilidade usando os três pilares fundamentais: **Logs, Métricas e Traces**.

## 📊 Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    APLICAÇÃO QUARKUS                        │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────┐     │
│  │  Logs    │    │ Métricas │    │  Traces (OTLP)   │     │
│  │  (JSON)  │    │   HTTP   │    │      gRPC        │     │
│  └────┬─────┘    └────┬─────┘    └────────┬─────────┘     │
└───────┼──────────────┼──────────────────────┼──────────────┘
        │              │                      │
        ▼              ▼                      ▼
   ┌─────────┐   ┌──────────┐        ┌──────────┐
   │ Promtail│   │Prometheus│        │  Jaeger  │
   └────┬────┘   └────┬─────┘        └────┬─────┘
        │             │                    │
        ▼             │                    │
   ┌─────────┐       │                    │
   │  Loki   │       │                    │
   └────┬────┘       │                    │
        │             │                    │
        └─────────────┴────────────────────┘
                      │
                      ▼
              ┌──────────────┐
              │   GRAFANA    │
              │   (UI única) │
              └──────────────┘
```

## 🎯 Componentes

| Componente | Propósito | Porta | Acesso |
|------------|-----------|-------|--------|
| **Loki** | Agregação de logs | 3100 | - |
| **Promtail** | Coleta logs dos containers | 9080 | - |
| **Prometheus** | Coleta e armazena métricas | 9090 | http://localhost:9090 |
| **Jaeger** | Rastreamento distribuído | 16686 | http://localhost:16686 |
| **Grafana** | Visualização unificada | 3000 | http://localhost:3000 |

### Por que usar cada um?

- **Loki**: Sistema de agregação de logs otimizado para armazenar e consultar logs de forma eficiente
- **Prometheus**: Banco de dados de séries temporais para métricas (CPU, memória, requests/s, etc)
- **Jaeger**: Rastreamento de requisições através de múltiplos serviços (distributed tracing)
- **Grafana**: Interface visual única para consultar logs, métricas e traces

## 📁 Estrutura do Projeto

```
.
├── docker-compose.yml
├── prometheus/
│   └── prometheus.yml
├── loki/
│   └── loki-config.yml
├── promtail/
│   └── promtail-config.yml
├── grafana/
│   └── provisioning/
│       └── datasources/
│           └── datasources.yml
└── README.md
```

## 🚀 Configuração

### 1. Docker Compose

Crie o arquivo `docker-compose.yml`:

```yaml
version: "3.3"
services:
  postgres_loan:
    image: postgres:latest
    environment:
      POSTGRES_USER: root
      POSTGRES_PASSWORD: root
    ports:
      - "5432:5432"

  postgres_commission:
    image: postgres:latest
    environment:
      POSTGRES_USER: root
      POSTGRES_PASSWORD: root
    ports:
      - "5433:5432"

  zookeeper:
    image: zookeeper:3.6.1
    container_name: zookeeper
    restart: always
    networks:
      - kafka-net
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka:2.12-2.5.0
    container_name: kafka
    restart: always
    networks:
      - kafka-net
    ports:
      - "9092:9092"
    environment:
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: DOCKER_INTERNAL:PLAINTEXT,DOCKER_EXTERNAL:PLAINTEXT
      KAFKA_LISTENERS: DOCKER_INTERNAL://:29092,DOCKER_EXTERNAL://:9092
      KAFKA_ADVERTISED_LISTENERS: DOCKER_INTERNAL://kafka:29092,DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: DOCKER_INTERNAL
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_BROKER_ID: 1
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper

  redis:
    image: "redis:6"
    ports:
      - 6379:6379
    networks:
      - kafka-net

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    restart: always
    networks:
      - kafka-net
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'

  loki:
    image: grafana/loki:latest
    container_name: loki
    restart: always
    networks:
      - kafka-net
    ports:
      - "3100:3100"
    volumes:
      - ./loki/loki-config.yml:/etc/loki/local-config.yaml
      - loki-data:/loki
    command: -config.file=/etc/loki/local-config.yaml

  promtail:
    image: grafana/promtail:latest
    container_name: promtail
    restart: always
    networks:
      - kafka-net
    volumes:
      - ./promtail/promtail-config.yml:/etc/promtail/config.yml
      - /var/log:/var/log
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock
    command: -config.file=/etc/promtail/config.yml
    depends_on:
      - loki

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    restart: always
    networks:
      - kafka-net
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    depends_on:
      - prometheus
      - loki

  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: jaeger
    restart: always
    networks:
      - kafka-net
    ports:
      - "5775:5775/udp"
      - "6831:6831/udp"
      - "6832:6832/udp"
      - "5778:5778"
      - "16686:16686"
      - "14268:14268"
      - "14250:14250"
      - "9411:9411"
      - "4317:4317"
      - "4318:4318"
    environment:
      - COLLECTOR_ZIPKIN_HOST_PORT=:9411
      - COLLECTOR_OTLP_ENABLED=true

networks:
  kafka-net:
    name: kafka-net
    driver: bridge

volumes:
  prometheus-data:
  grafana-data:
  loki-data:
```

### 2. Configuração do Prometheus

Crie `./prometheus/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'jaeger'
    static_configs:
      - targets: ['jaeger:14269']

  # Aplicações Quarkus
  - job_name: 'quarkus-apps'
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['host.docker.internal:8080']
        labels:
          app: 'loan-service'
      - targets: ['host.docker.internal:8081']
        labels:
          app: 'commission-service'
```

### 3. Configuração do Loki

Crie `./loki/loki-config.yml`:

```yaml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
    final_sleep: 0s
  chunk_idle_period: 5m
  chunk_retain_period: 30s

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 168h

storage_config:
  boltdb:
    directory: /loki/index

  filesystem:
    directory: /loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h

chunk_store_config:
  max_look_back_period: 0s

table_manager:
  retention_deletes_enabled: false
  retention_period: 0s
```

### 4. Configuração do Promtail

Crie `./promtail/promtail-config.yml`:

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: ['__meta_docker_container_name']
        regex: '/(.*)'
        target_label: 'container'
      - source_labels: ['__meta_docker_container_log_stream']
        target_label: 'logstream'
      - source_labels: ['__meta_docker_container_label_com_docker_compose_service']
        target_label: 'service'
    pipeline_stages:
      - json:
          expressions:
            level: level
            message: message
            app: app
            timestamp: timestamp
      - labels:
          level:
          app:
```

### 5. Configuração de Datasources do Grafana

Crie `./grafana/provisioning/datasources/datasources.yml`:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: true

  - name: Jaeger
    type: jaeger
    access: proxy
    url: http://jaeger:16686
    editable: true
```

## 🔧 Integração com Quarkus

### 1. Dependências Maven

Adicione no `pom.xml`:

```xml
<!-- Métricas para Prometheus -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Logs estruturados (JSON) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-logging-json</artifactId>
</dependency>

<!-- OpenTelemetry para Traces (Jaeger) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry</artifactId>
</dependency>

<!-- Health checks -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

### 2. Configuração application.properties

```properties
# Nome da aplicação
quarkus.application.name=minha-app

# ===== MÉTRICAS - PROMETHEUS =====
quarkus.micrometer.enabled=true
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.http-client.enabled=true
quarkus.micrometer.binder.jvm=true
quarkus.micrometer.binder.system=true

# ===== LOGS - LOKI (formato JSON) =====
quarkus.log.console.format=%d{HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n
quarkus.log.console.json=true
quarkus.log.console.json.pretty-print=false
quarkus.log.console.json.additional-field."app".value=${quarkus.application.name}
quarkus.log.console.json.additional-field."host".value=${HOST:localhost}

# ===== TRACES - JAEGER (via OpenTelemetry) =====
quarkus.otel.enabled=true
quarkus.otel.exporter.otlp.traces.endpoint=http://localhost:4317
quarkus.otel.exporter.otlp.traces.protocol=grpc
quarkus.otel.service.name=${quarkus.application.name}
quarkus.otel.traces.sampler=always_on
quarkus.otel.propagators=tracecontext,baggage

# ===== HEALTH CHECKS =====
quarkus.health.enabled=true
```

### 3. Exemplo de Código

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/exemplo")
public class ExemploResource {

    private static final Logger LOG = Logger.getLogger(ExemploResource.class);

    @Inject
    MeterRegistry registry;

    @Inject
    Tracer tracer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        // Log (enviado para Loki)
        LOG.info("Processando requisição hello");

        // Métrica customizada (enviada para Prometheus)
        Counter counter = registry.counter("exemplo.hello.count");
        counter.increment();

        // Trace customizado (enviado para Jaeger)
        Span span = tracer.spanBuilder("operacao-customizada").startSpan();
        try {
            Thread.sleep(100);
            LOG.info("Operação executada com sucesso");
            return "Hello from Quarkus!";
        } catch (InterruptedException e) {
            span.recordException(e);
            LOG.error("Erro na operação", e);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
}
```

## 🎬 Como Usar

### 1. Iniciar a Stack

```bash
# Criar os diretórios necessários
mkdir -p prometheus loki promtail grafana/provisioning/datasources

# Subir todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps
```

### 2. Iniciar Aplicação Quarkus

```bash
./mvnw clean quarkus:dev
```

### 3. Verificar Endpoints

```bash
# Métricas
curl http://localhost:8080/q/metrics

# Health Check
curl http://localhost:8080/q/health

# Sua aplicação
curl http://localhost:8080/exemplo
```

### 4. Acessar Interfaces

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | - |
| Jaeger UI | http://localhost:16686 | - |

## 📊 Usando o Grafana

### Ver Logs (Loki)

1. Acesse Grafana → Explore
2. Selecione datasource **Loki**
3. Use queries como:
   ```
   {app="minha-app"}
   {container="kafka"}
   {level="ERROR"}
   ```

### Ver Métricas (Prometheus)

1. Acesse Grafana → Explore
2. Selecione datasource **Prometheus**
3. Use queries como:
   ```
   http_server_requests_seconds_count
   jvm_memory_used_bytes
   exemplo_hello_count_total
   ```

### Ver Traces (Jaeger)

1. Acesse Grafana → Explore
2. Selecione datasource **Jaeger**
3. Ou acesse direto: http://localhost:16686
4. Busque pelo serviço: `minha-app`

## 🔍 Queries Úteis

### Loki (Logs)

```logql
# Todos os logs de erro
{level="ERROR"}

# Logs de uma aplicação específica
{app="minha-app"} |= "exception"

# Logs nos últimos 5 minutos com filtro
{container="kafka"} | json | level="WARN"
```

### Prometheus (Métricas)

```promql
# Taxa de requisições HTTP
rate(http_server_requests_seconds_count[5m])

# Uso de memória JVM
jvm_memory_used_bytes{area="heap"}

# Percentil 95 de latência
histogram_quantile(0.95, http_server_requests_seconds_bucket)
```

### Jaeger (Traces)

- Busque por serviço: `minha-app`
- Filtre por operação: `GET /exemplo`
- Veja a duração e spans detalhados

## 🛠️ Troubleshooting

### Logs não aparecem no Loki

```bash
# Verificar logs do Promtail
docker logs promtail

# Verificar se Loki está recebendo dados
curl http://localhost:3100/ready

# Verificar configuração
docker exec -it promtail cat /etc/promtail/config.yml
```

### Métricas não aparecem no Prometheus

```bash
# Verificar endpoint de métricas da app
curl http://localhost:8080/q/metrics

# Verificar targets no Prometheus
# Acesse: http://localhost:9090/targets

# Verificar logs do Prometheus
docker logs prometheus
```

### Traces não aparecem no Jaeger

```bash
# Verificar se Jaeger está recebendo dados
curl http://localhost:16686/api/services

# Verificar logs do Jaeger
docker logs jaeger

# Verificar configuração OTLP da aplicação
curl http://localhost:8080/q/health
```

### Problemas com Docker no Windows/Mac

Se usar `host.docker.internal` não funcionar, substitua por:
- **Linux**: IP da interface docker0 (geralmente `172.17.0.1`)
- **Windows/Mac**: `host.docker.internal` deve funcionar

## 📚 Recursos Adicionais

- [Documentação Quarkus OpenTelemetry](https://quarkus.io/guides/opentelemetry)
- [Documentação Quarkus Micrometer](https://quarkus.io/guides/micrometer)
- [Loki Query Language (LogQL)](https://grafana.com/docs/loki/latest/logql/)
- [PromQL Basics](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)

## 🎯 Melhores Práticas

1. **Logs Estruturados**: Sempre use formato JSON para facilitar queries
2. **Métricas Customizadas**: Crie métricas de negócio relevantes
3. **Sampling de Traces**: Em produção, use sampling menor que 100%
4. **Retenção de Dados**: Configure retenção adequada no Loki e Prometheus
5. **Alertas**: Configure alertas no Grafana para métricas críticas
6. **Dashboards**: Crie dashboards específicos para cada serviço

## 📝 Licença

Este projeto é de código aberto e está disponível sob a licença MIT.