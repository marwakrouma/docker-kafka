package fr.test;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Sample extends AbstractSample {

    static final String TOPIC_IN = "topic_in";
    static final String TOPIC_OUT = "topic_out";

    protected StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(TOPIC_IN, Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)).grace(ofSeconds(5)))
                .count()
                .suppress(Suppressed.untilWindowCloses(unbounded()))
                .toStream()
                .map((k, v) -> KeyValue.pair(k.key(), v.toString()))
                .to(TOPIC_OUT, Produced.with(Serdes.String(), Serdes.String()));
        return builder;
    }


    public static void main(final String[] args) {
        new SpringApplication(Sample.class)
                .run(args)
                .registerShutdownHook();
    }

}
