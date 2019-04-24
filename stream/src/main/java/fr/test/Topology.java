package fr.test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import avro.shaded.com.google.common.collect.Maps;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.StreamsBuilder;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Topology<T extends SpecificRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Topology.class);
    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    private static final String SPECIFIC_AVRO_READER = "specific.avro.reader";

    private SpecificAvroSerde<T> specificAvroSerde;

    abstract public StreamsBuilder getTopology();

    public Properties getProperties() {
        final Properties props = new Properties();
        try {
            props.load(this.getClass().getResourceAsStream("/application-" + getApplicationName() + ".properties"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info(props.toString());
        return props;
    }

    public void setSpecificAvroSerde(SpecificAvroSerde<T> specificAvroSerde) {
        if (getProperties().getProperty(SCHEMA_REGISTRY_URL) != null && getProperties().getProperty(SPECIFIC_AVRO_READER) != null) {
            Map<String, String> config = Maps.newHashMap();
            config.put(SCHEMA_REGISTRY_URL, getProperties().getProperty(SCHEMA_REGISTRY_URL));
            config.put(SPECIFIC_AVRO_READER, getProperties().getProperty(SPECIFIC_AVRO_READER));

            specificAvroSerde.configure(config, false);
        }
        this.specificAvroSerde = specificAvroSerde;
    }

    public String getApplicationName() {
        String simpleName = this.getClass().getSimpleName();
        LOGGER.info("application properties name => " + simpleName);
        return simpleName;
    }

    public SpecificAvroSerde<T> getSpecificAvroSerde() {
        return specificAvroSerde;
    }
}
