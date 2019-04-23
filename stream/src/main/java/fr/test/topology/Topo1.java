package fr.test.topology;

import fr.test.Topology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

/**
 * Topology qui fait le count sur une fenetre de temps de 1 minute.
 * L'utilisation de l'operateur suppress permet d'émettre uniquement le dernier evenement pour chaque clef de la fenetre de temps
 */
@Component
public class Topo1 implements Topology {

    static final String TOPIC_IN = "topic_in";
    static final String TOPIC_OUT = "topic_out";

    public StreamsBuilder getTopology() {
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

    @Override
    public String getApplicationId() {
        return "topo1";
    }
}
