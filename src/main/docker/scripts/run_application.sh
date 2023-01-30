#!/bin/sh

# output some debug info first
id
env
pwd

/usr/bin/java \
  -javaagent:/app/lib/jmx_prometheus_javaagent-0.17.2.jar=1234:/app/config/jmx_exporter_kafka_client.yml \
  -Xms64m -Xmx256m \
  -cp /app/lib/ \
  -jar /app/lib/$1.jar

# not used for Confluent Cloud example:
#  --enable-monitoring-interceptor
