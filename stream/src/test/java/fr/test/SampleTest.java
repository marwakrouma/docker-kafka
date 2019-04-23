package fr.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Before;
import org.junit.Test;

public class SampleTest {

    private static final String TARGET_TOPOLOGY_DESCRIPTION = "target/topology-description";

    private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(Serdes.String().serializer(), Serdes.String().serializer());

    private Sample sample;

    @Before
    public void before() {
        sample = new Sample();
    }

    @Test
    public void test() throws IOException {
        TopologyTestDriver ttd = new TopologyTestDriver(sample.getTopology().build(), sample.getProperties());
        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();

        Instant now = Instant.now();
        System.out.println("now => " + now + " => " + now.toEpochMilli());

        records.add(createRecord("1", now));
        records.add(createRecord("1", now.plusSeconds(2)));
        records.add(createRecord("2", now.plusSeconds(2)));
        records.add(createRecord("1", now.plusSeconds(5)));
        records.add(createRecord("3", now.plusSeconds(30)));
        records.add(createRecord("1", now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS)));
        records.add(createRecord("2", now.plus(3, ChronoUnit.MINUTES)));

        ttd.pipeInput(records);

        List<ProducerRecord> results = new ArrayList<>();
        ProducerRecord<String, String> record;
        while ((record = ttd.readOutput(Sample.TOPIC_OUT, Serdes.String().deserializer(), Serdes.String().deserializer())) != null) {
            results.add(record);
        }

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(Sample.TOPIC_OUT, null, now.plusSeconds(5).toEpochMilli(), "1","3"),
            new ProducerRecord<>(Sample.TOPIC_OUT, null, now.plusSeconds(2).toEpochMilli(), "2","1"),
            new ProducerRecord<>(Sample.TOPIC_OUT, null, now.plusSeconds(30).toEpochMilli(), "3", "1"),
            new ProducerRecord<>(Sample.TOPIC_OUT, null, now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS).toEpochMilli(), "1", "1")
        ));
    }


    @Test
    public void generateTopologyFiles() {
        toFile(SampleTest.class.getName() + ".txt", sample.getTopology().build().describe().toString());
    }

    private void toFile(String fileName, String content) {
        Path topologyFile = Paths.get(TARGET_TOPOLOGY_DESCRIPTION + "/" + fileName);
        try {
            Files.createDirectories(Paths.get(TARGET_TOPOLOGY_DESCRIPTION));
            Files.deleteIfExists(topologyFile);
            Path file = Files.createFile(topologyFile);
            Files.write(file, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConsumerRecord<byte[], byte[]> createRecord(String key, Instant now) {
        System.out.println("timestampMs => " + now + " => "  + now.toEpochMilli());
        return recordFactory.create(Sample.TOPIC_IN, key, "test", now.toEpochMilli());
    }
}
