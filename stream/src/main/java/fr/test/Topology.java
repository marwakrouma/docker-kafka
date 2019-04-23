package fr.test;

import org.apache.kafka.streams.StreamsBuilder;

import java.io.IOException;
import java.util.Properties;

public interface Topology {

    StreamsBuilder getTopology();

    default Properties getProperties() throws IOException {
        final Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/application-" + getApplicationId() + ".properties"));
        return props;
    }


    String getApplicationId();
}
