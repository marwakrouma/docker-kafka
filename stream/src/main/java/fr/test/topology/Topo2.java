package fr.test.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import fr.test.Topology;

/**
 * Topology qui concatene la key et value dans une nouvelle valeur 'key -> value'
 */
@Component
public class Topo2 extends Topology {

    static final String TOPIC_IN = "topic_out";
    static final String TOPIC_OUT = "topic_out_1";

    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(TOPIC_IN, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues((k, v) -> k + " -> " + v)
                .to(TOPIC_OUT, Produced.with(Serdes.String(), Serdes.String()));
        return builder;
    }
}



