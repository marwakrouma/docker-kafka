package fr.test.topology;

import static java.util.Collections.singletonMap;
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
import fr.test.model.Message;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class Topo3Test extends AbstractTopologyTest {

    private SpecificAvroSerde specificAvroSerde;

    @Before
    public void before() {
        specificAvroSerde = before(new Topo3(), singletonMap(Topo3.TOPIC_OUT + "-value", Message.getClassSchema()));
    }

    @Test
    public void test() {
        Instant now = Instant.now();

        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();
        records.add(createRecord(Topo3.TOPIC_IN, "1", "1 -> value", now));
        records.add(createRecord(Topo3.TOPIC_IN, "2", "2 -> value", now.plusSeconds(2)));

        pipeInput(records);

        List<ProducerRecord> results = readOutput(Topo3.TOPIC_OUT, Serdes.String().deserializer(), specificAvroSerde.deserializer());

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(Topo3.TOPIC_OUT, null, now.toEpochMilli(), "1", Message.newBuilder().setId("1").setCount("1 -> value").build()),
            new ProducerRecord<>(Topo3.TOPIC_OUT, null, now.plusSeconds(2).toEpochMilli(), "2",Message.newBuilder().setId("2").setCount("2 -> value").build())
        ));
    }
}
