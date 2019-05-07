package fr.test.topology;

import fr.test.Topology;
import fr.test.model.Iptv;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;
import org.apache.kafka.streams.kstream.KStream;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;

/**
 * Topology qui transforme Json en Avro
 */
@Component
public class IptvEventToAvro extends Topology {

    static final String TOPIC_IN = "iptv_event";
    static final String TOPIC_OUT = "iptv_event_avro";

    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        final ObjectMapper objectMapper = new ObjectMapper();
        // read the source stream
        final KStream<String, String> jsonToAvroStream = builder.stream(TOPIC_IN,
                Consumed.with(Serdes.String(), Serdes.String()));
        jsonToAvroStream.mapValues(v -> {
            Iptv iptv = null;
            try {
                final JsonNode jsonNode = objectMapper.readTree(v);
                iptv = new Iptv(jsonNode.get("mac").asText(),
                        jsonNode.get("zip").asText(),
                        jsonNode.get("groupid").asText(),
                        jsonNode.get("lzd").asText(),
                        jsonNode.get("lkp").asText()

                );
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return iptv;
        }).filter((k,v) -> v != null).to(TOPIC_OUT, Produced.with(Serdes.String(), getSpecificAvroSerde()) );

        return builder;
    }
}



