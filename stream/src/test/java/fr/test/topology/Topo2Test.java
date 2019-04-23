package fr.test.topology;

import fr.test.AbstractTopologyTest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Topo2Test extends AbstractTopologyTest {

    @Before
    public void before() {
        topology = new Topo2();
    }

    @Test
    public void test() throws IOException {
        TopologyTestDriver ttd = new TopologyTestDriver(topology.getTopology().build(), topology.getProperties());
        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();

        Instant now = Instant.now();
        System.out.println("now => " + now + " => " + now.toEpochMilli());

        records.add(createRecord(Topo2.TOPIC_IN, "1", "value", now));
        records.add(createRecord(Topo2.TOPIC_IN, "2", "value", now.plusSeconds(2)));

        ttd.pipeInput(records);

        List<ProducerRecord> results = new ArrayList<>();
        ProducerRecord<String, String> record;
        while ((record = ttd.readOutput(Topo2.TOPIC_OUT, Serdes.String().deserializer(), Serdes.String().deserializer())) != null) {
            results.add(record);
        }

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(Topo2.TOPIC_OUT, null, now.toEpochMilli(), "1","1 -> value"),
            new ProducerRecord<>(Topo2.TOPIC_OUT, null, now.plusSeconds(2).toEpochMilli(), "2","2 -> value")
        ));
    }
}
