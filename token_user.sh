 curl -X POST http://localhost:8180/realms/quarkus-realm/protocol/openid-connect/token \                                                                      -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=quarkus-app" \
  -d "client_secret=test" \
  -d "username=joao" \
  -d "password=senha123" \
  -d "grant_type=password"