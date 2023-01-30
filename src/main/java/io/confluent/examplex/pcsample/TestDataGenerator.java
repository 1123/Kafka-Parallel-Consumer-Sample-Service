package io.confluent.examplex.pcsample;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED;
import static java.util.List.of;

@Slf4j
public class TestDataGenerator {

    private static final String INPUT_TOPIC = "pc-sample-input-topic";

    private Producer<String, String> getKafkaProducer() throws IOException {
        Properties properties = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("producer.properties");
        properties.load(stream);
        return new KafkaProducer<>(properties);
    }

    public static void main(String ... args) {


    }

}
