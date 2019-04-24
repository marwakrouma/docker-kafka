package fr.test.topology;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.Before;
import org.junit.Test;

import fr.test.AbstractTopologyTest;

public class Topo1Test extends AbstractTopologyTest {

    @Before
    public void before() {
        before(new Topo1());
    }

    @Test
    public void test() {
        Instant now = Instant.now();

        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();
        records.add(createRecord(Topo1.TOPIC_IN, "1", now));
        records.add(createRecord(Topo1.TOPIC_IN, "1", now.plusSeconds(2)));
        records.add(createRecord(Topo1.TOPIC_IN, "2", now.plusSeconds(2)));
        records.add(createRecord(Topo1.TOPIC_IN, "1", now.plusSeconds(5)));
        records.add(createRecord(Topo1.TOPIC_IN, "3", now.plusSeconds(30)));
        records.add(createRecord(Topo1.TOPIC_IN, "1", now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS)));
        records.add(createRecord(Topo1.TOPIC_IN, "2", now.plus(3, ChronoUnit.MINUTES)));
        pipeInput(records);

        List<ProducerRecord> results = readOutput(Topo1.TOPIC_OUT, Serdes.String().deserializer(), Serdes.String().deserializer());

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(Topo1.TOPIC_OUT, null, now.plusSeconds(5).toEpochMilli(), "1","3"),
            new ProducerRecord<>(Topo1.TOPIC_OUT, null, now.plusSeconds(2).toEpochMilli(), "2","1"),
            new ProducerRecord<>(Topo1.TOPIC_OUT, null, now.plusSeconds(30).toEpochMilli(), "3", "1"),
            new ProducerRecord<>(Topo1.TOPIC_OUT, null, now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS).toEpochMilli(), "1", "1")
        ));
    }
}
