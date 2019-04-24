package fr.test.topology;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.Before;
import org.junit.Test;

import fr.test.AbstractTopologyTest;

public class Topo2Test extends AbstractTopologyTest {

    @Before
    public void before() {
        before(new Topo2());
    }

    @Test
    public void test() {
        Instant now = Instant.now();

        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();
        records.add(createRecord(Topo2.TOPIC_IN, "1", "value", now));
        records.add(createRecord(Topo2.TOPIC_IN, "2", "value", now.plusSeconds(2)));
        pipeInput(records);

        List<ProducerRecord> results = readOutput(Topo2.TOPIC_OUT, Serdes.String().deserializer(), Serdes.String().deserializer());

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(Topo2.TOPIC_OUT, null, now.toEpochMilli(), "1","1 -> value"),
            new ProducerRecord<>(Topo2.TOPIC_OUT, null, now.plusSeconds(2).toEpochMilli(), "2","2 -> value")
        ));
    }
}
