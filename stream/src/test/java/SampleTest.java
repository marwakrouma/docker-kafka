import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.Test;

public class SampleTest {

    private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(Serdes.String().serializer(), Serdes.String().serializer());

    @Test
    public void test() {
        TopologyTestDriver ttd = new TopologyTestDriver(Sample.getTopology().build(), Sample.getProperties());
        List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>();

        Instant now = Instant.now();
        System.out.println("now => " + now + " => " + now.toEpochMilli());
        Instant nowTruncatedTo = now.truncatedTo(ChronoUnit.MINUTES);
        System.out.println("nowTruncatedTo => " + nowTruncatedTo + " => " + nowTruncatedTo.toEpochMilli());

        records.add(createRecord("1", now));
        records.add(createRecord("1", now.plusSeconds(2)));
        records.add(createRecord("2", now.plusSeconds(2)));
        records.add(createRecord("1", now.plusSeconds(5)));
        records.add(createRecord("3", now.plusSeconds(30)));
        records.add(createRecord("1", now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS)));
        records.add(createRecord("2", now.plus(3, ChronoUnit.MINUTES)));

        ttd.pipeInput(records);
        List<ProducerRecord> results = new ArrayList<>();
        ProducerRecord<String, Long> record;
        while ((record = ttd.readOutput(Sample.TOPIC_OUT, Serdes.String().deserializer(), Serdes.Long().deserializer())) != null) {
            results.add(record);
        }

        assertThat(results).containsOnlyElementsOf(Arrays.asList(
            new ProducerRecord<>(
                Sample.TOPIC_OUT,
                null,
                now.plusSeconds(5).toEpochMilli(),
                "Window{startMs=" + nowTruncatedTo.toEpochMilli() + ", endMs=" + nowTruncatedTo.plus(1, ChronoUnit.MINUTES).toEpochMilli() + "}" + " 1",
                3L),
            new ProducerRecord<>(
                Sample.TOPIC_OUT,
                null,
                now.plusSeconds(2).toEpochMilli(),
                "Window{startMs=" + nowTruncatedTo.toEpochMilli() + ", endMs=" + nowTruncatedTo.plus(1, ChronoUnit.MINUTES).toEpochMilli() + "}" + " 2",
                1L),
            new ProducerRecord<>(
                Sample.TOPIC_OUT,
                null,
                now.plusSeconds(30).toEpochMilli(),
                "Window{startMs=" + nowTruncatedTo.toEpochMilli() + ", endMs=" + nowTruncatedTo.plus(1, ChronoUnit.MINUTES).toEpochMilli() + "}" + " 3",
                1L),
            new ProducerRecord<>(
                Sample.TOPIC_OUT,
                null,
                now.plus(2, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS).toEpochMilli(),
                "Window{startMs=" + nowTruncatedTo.plus(2, ChronoUnit.MINUTES).toEpochMilli() + ", endMs=" + nowTruncatedTo.plus(2, ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES).toEpochMilli() + "}" + " 1",
                1L)
        ));
    }

    public ConsumerRecord<byte[], byte[]> createRecord(String key, Instant now) {
        System.out.println("timestampMs => " + now + " => "  + now.toEpochMilli());
        return recordFactory.create(Sample.TOPIC_IN, key, "test", now.toEpochMilli());
    }
}
