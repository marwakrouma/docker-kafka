package fr.test;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractSample implements CommandLineRunner {

    @Override
    public void run(String... args) throws IOException {
        final Properties streamsConfiguration = getProperties();

        final StreamsBuilder builder = getTopology();

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    protected abstract StreamsBuilder getTopology();

    protected Properties getProperties() throws IOException {
        final Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/application.properties"));
        return props;
    }
}
