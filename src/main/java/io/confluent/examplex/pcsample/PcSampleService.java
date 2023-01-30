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
import java.util.Random;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED;
import static java.util.List.of;

@Slf4j
public class PcSampleService {

    Random r = new Random();

    private static final String INPUT_TOPIC_1 = "pc-sample-input-topic";
    private static final String INPUT_TOPIC_2 = "pc-sample-input-topic-2";

    private Consumer<String, String> getKafkaConsumer() throws IOException {
        Properties properties = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer.properties");
        properties.load(stream);
        return new KafkaConsumer<>(properties);
    }

    private Producer<String, String> getKafkaProducer() throws IOException {
        Properties properties = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("producer.properties");
        properties.load(stream);
        return new KafkaProducer<>(properties);
    }

    private ParallelStreamProcessor<String, String> getParallelStreamProcessor() throws IOException {
        Consumer<String, String> kafkaConsumer = getKafkaConsumer();
        Producer<String, String> kafkaProducer = getKafkaProducer();

        var options = ParallelConsumerOptions.<String, String>builder()
                .ordering(UNORDERED)
                .maxConcurrency(5)
                .consumer(kafkaConsumer)
                .producer(kafkaProducer)
                .build();

        ParallelStreamProcessor<String, String> eosStreamProcessor =
                ParallelStreamProcessor.createEosStreamProcessor(options);

        eosStreamProcessor.subscribe(of(INPUT_TOPIC_1, INPUT_TOPIC_2));

        return eosStreamProcessor;
    }

    public void run() throws IOException {
        ParallelStreamProcessor<String, String> processor = getParallelStreamProcessor();
        processor.poll(record -> {
            log.info("Start processing record: {}", record);
            try {
                // TODO: simulate failure of message processing.
                int waittime = r.nextInt(5 * 1000);

                Thread.sleep(waittime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("End processing record: {}", record);
        });
    }

}
