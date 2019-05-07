package fr.test.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import fr.test.Topology;
import fr.test.model.Client;
import org.apache.kafka.streams.kstream.KStream;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;

/**
 * Topology qui transforme Json en Avro
 */
@Component
public class RefClientToAvro extends Topology {

    static final String TOPIC_IN = "ref_client";
    static final String TOPIC_OUT = "ref_client_avro";

    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        final ObjectMapper objectMapper = new ObjectMapper();
        // read the source stream
        final KStream<String, String> jsonToAvroStream = builder.stream(TOPIC_IN,
                Consumed.with(Serdes.String(), Serdes.String()));
        jsonToAvroStream.mapValues(v -> {
            Client client = null;
            try {
                final JsonNode jsonNode = objectMapper.readTree(v);
                client = new Client(jsonNode.get("mac").asText(),
                        jsonNode.get("name").asText(),
                        jsonNode.get("city").asText()
                );
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return client;
        }).filter((k,v) -> v != null).to(TOPIC_OUT, Produced.with(Serdes.String(), getSpecificAvroSerde()) );

        return builder;
    }
}



