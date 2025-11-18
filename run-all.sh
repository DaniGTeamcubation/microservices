#!/bin/bash
export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java:"
java -version

echo "=== Starting Eureka Server ==="
(cd discovery-service && mvn spring-boot:run) &
sleep 15

echo "=== Starting API Gateway ==="
(cd api-gateway && mvn spring-boot:run) &
sleep 10

echo "=== Starting Member Service ==="
(cd member-service && mvn spring-boot:run) &

echo "=== Starting Claim Service ==="
(cd claim-service && mvn spring-boot:run) &

echo "=== All services started ==="
wait