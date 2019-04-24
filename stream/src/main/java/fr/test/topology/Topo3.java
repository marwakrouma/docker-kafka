package fr.test.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import fr.test.Topology;
import fr.test.model.Message;

/**
 * Topology qui transforme String en Avro
 */
@Component
public class Topo3 extends Topology {

    static final String TOPIC_IN = "topic_out_1";
    static final String TOPIC_OUT = "topic_out_2";

    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(TOPIC_IN, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues((k, v) -> Message.newBuilder().setId(k).setCount(v).build())
                .to(TOPIC_OUT, Produced.with(Serdes.String(), getSpecificAvroSerde()));
        return builder;
    }
}



