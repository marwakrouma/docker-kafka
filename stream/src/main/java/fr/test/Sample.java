package fr.test;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Sample implements CommandLineRunner {

    static final String TOPIC_IN = "topic_in";
    static final String TOPIC_OUT = "topic_out";

    @Override
    public void run(String... args) throws IOException {
        final Properties streamsConfiguration = getProperties();

        final StreamsBuilder builder = getTopology();

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    Properties getProperties() throws IOException {
        final Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/application.properties"));
        return props;
    }

    StreamsBuilder getTopology() {
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
