package io.confluent.examplex.pcsample;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import io.confluent.parallelconsumer.vertx.JStreamVertxParallelStreamProcessor;
import io.confluent.parallelconsumer.vertx.VertxParallelEoSStreamProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED;
import static java.util.List.of;

@Slf4j
public class PcVertexSample {

    private static final String INPUT_TOPIC_1 = "pc-sample-input-topic";

    @SneakyThrows
    private Consumer<String, String> getKafkaConsumer()  {
        Properties properties = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer.properties");
        properties.load(stream);
        return new KafkaConsumer<>(properties);
    }

    @SneakyThrows
    private Producer<String, String> getKafkaProducer() {
        Properties properties = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("producer.properties");
        properties.load(stream);
        return new KafkaProducer<>(properties);
    }


    JStreamVertxParallelStreamProcessor<String, String> parallelConsumer;

    void run() {
        Consumer<String, String> kafkaConsumer = getKafkaConsumer();
        Producer<String, String> kafkaProducer = getKafkaProducer();
        var options = ParallelConsumerOptions.<String, String>builder()
                .ordering(UNORDERED)
                .consumer(kafkaConsumer)
                .producer(kafkaProducer)
                .build();

        this.parallelConsumer = JStreamVertxParallelStreamProcessor.createEosStreamProcessor(options);
        parallelConsumer.subscribe(of(INPUT_TOPIC_1));

        int port = 8080;

        // tag::example[]
        var resultStream = parallelConsumer.vertxHttpReqInfoStream(context -> {
            var consumerRecord = context.getSingleConsumerRecord();
            log.info("Concurrently constructing and returning RequestInfo from record: {}", consumerRecord);
            Map<String, String> params = Map.of("recordKey", consumerRecord.key(), "payload", consumerRecord.value());
            return new VertxParallelEoSStreamProcessor.RequestInfo("localhost", port, "/", params); // <1>
        });
        // end::example[]

        resultStream.forEach(x -> {
            log.info("From result stream: {}", x);
        });

    }
}
