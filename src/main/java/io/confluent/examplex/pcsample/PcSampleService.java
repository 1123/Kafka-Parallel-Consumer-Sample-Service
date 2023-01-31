package io.confluent.examplex.pcsample;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED;
import static java.util.List.of;

@Slf4j
public class PcSampleService {

    Random r = new Random();

    private static final String INPUT_TOPIC_1 = "pc-sample-input-topic";
    private static final String INPUT_TOPIC_2 = "pc-sample-input-topic-2";

    private static final String OUTPUT_TOPIC = "pc-sample-output-topic";

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

        eosStreamProcessor.subscribe(of("numbers"));

        return eosStreamProcessor;
    }


    List<String> failedMessages = new ArrayList<>();

    public void run() throws IOException {
        ParallelStreamProcessor<String, String> processor = getParallelStreamProcessor();

        processor.pollAndProduce(context -> {

            var consumerRecord = context.getSingleRecord().getConsumerRecord();
            log.info("Start processing record: {}", consumerRecord);
                    int waittime = r.nextInt(5 * 1000);
                    String partitionAndOffset = String.format("p-%d-o-%d", consumerRecord.partition(), consumerRecord.offset());
                    if (failedMessages.contains(partitionAndOffset)) {
                        log.info("Reprocessing of failed message {}", partitionAndOffset);
                        processor.close();
                        System.exit(0);
                    }
                    try {
                        if (waittime % 5 == 0) {
                            failedMessages.add(partitionAndOffset);
                            throw new RuntimeException(String.format("This message cannot be processed: %s", partitionAndOffset));
                        }
                        Thread.sleep(waittime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("End processing record: {}", consumerRecord);
                    return new ProducerRecord<>(OUTPUT_TOPIC, consumerRecord.key(), "{ \"metadata\" : {} }");
                }, consumeProduceResult -> {
                    log.debug("Message {} saved to broker at offset {}",
                            consumeProduceResult.getOut(),
                            consumeProduceResult.getMeta().offset());
                }
        );

    }

}

class UnprocessableMessageException extends RuntimeException {

}