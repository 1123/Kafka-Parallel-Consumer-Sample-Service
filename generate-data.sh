for number in $(seq 1 100); do     
  echo $number 
done | kafka-console-producer --bootstrap-server pkc-75m1o.europe-west3.gcp.confluent.cloud:9092 --producer.config src/main/resources/producer.properties --topic numbers 

