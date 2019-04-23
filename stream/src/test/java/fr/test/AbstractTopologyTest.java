package fr.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public abstract class AbstractTopologyTest {

    private static final String TARGET_TOPOLOGY_DESCRIPTION = "target/topology-description";

    private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(Serdes.String().serializer(), Serdes.String().serializer());


    public Topology topology;

    @Test
    public void generateTopologyFiles() {
        toFile(this.getClass().getName() + ".txt", topology.getTopology().build().describe().toString());
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

    public ConsumerRecord<byte[], byte[]> createRecord(String topic, String key, Instant now) {
        return createRecord(topic, key, "test", now);
    }

    public ConsumerRecord<byte[], byte[]> createRecord(String topic, String key, String value, Instant now) {
        System.out.println("timestampMs => " + now + " => "  + now.toEpochMilli());
        return recordFactory.create(topic, key, value, now.toEpochMilli());
    }

}
