#!/bin/bash

# Get container IP address on eth0
CONTAINER_IP=$(hostname -i | awk '{print $1}')
echo "Configuring Nginx to bind to Container IP: $CONTAINER_IP:8080"
sed -i "s/CONTAINER_IP/$CONTAINER_IP/g" /etc/nginx/sites-enabled/default

# Start SSH service
service ssh start

# Start Nginx
service nginx start

# Wait for MySQL database to be ready
echo "Waiting for database connection on host 'db'..."
until mysqladmin ping -h"db" -u"root" -p"Hello@123" --silent; do
    echo "Database is not ready yet. Retrying in 2 seconds..."
    sleep 2
done
echo "Database is ready! Starting Spring Boot..."

# Run Spring Boot app on port 8080 binding to localhost
cd /app
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar --server.port=8080 --server.address=127.0.0.1
