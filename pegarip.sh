# Opção 1: IP da interface docker0
ip addr show docker0 | grep "inet " | awk '{print $2}' | cut -d/ -f1

# Opção 2: Gateway da rede kafka-net
docker network inspect kafka-net | grep Gateway

# Normalmente o IP é: 172.17.0.1