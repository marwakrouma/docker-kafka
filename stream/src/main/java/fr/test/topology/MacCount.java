package fr.test.topology;

import fr.test.Topology;
import fr.test.model.Iptv;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.HashSet;

import fr.test.serdes.HashsetSerdes.SpanNamesDeserializer;
import fr.test.serdes.HashsetSerdes.SpanNamesSerializer;



import static java.time.Duration.ofSeconds;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

/**
 * Topology qui fait le count sur une fenetre de temps de 1 minute.
 * L'utilisation de l'operateur suppress permet d'émettre uniquement le dernier evenement pour chaque clef de la fenetre de temps
 */


@Component
public class MacCount extends Topology {

    static final String IPTV_TOPIC = "iptv_event_avro";
    static final String TOPIC_OUT = "mac_count";
    static final String Client_STORE_COUNT = "client_store_count";

    SpanNamesSerializer  setSerializer = new SpanNamesSerializer();
    SpanNamesDeserializer setDeserializer = new SpanNamesDeserializer();
    Serde<Set<String>> SetSerde = Serdes.serdeFrom(setSerializer,setDeserializer);

    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Iptv> iptvStream = builder.stream(IPTV_TOPIC, Consumed.with(Serdes.String(), getSpecificAvroSerde()));


        final KGroupedStream<String, Iptv> channels = iptvStream
                .selectKey((mac, iptv ) -> iptv.getGroupid())
                .groupByKey(Grouped.with(Serdes.String(), getSpecificAvroSerde()));

        final KTable<Windowed<String>, Integer> aggregatedStream = channels
                .windowedBy(TimeWindows.of(60 * 1000L).grace(ofSeconds(5)))
                .aggregate((Initializer<Set<String>>) HashSet::new,(k, v, current) -> {current.add(v.getMac()); return current;},
                Materialized.<String, Set<String>, WindowStore<Bytes, byte[]>>as(Client_STORE_COUNT).withValueSerde(SetSerde))
                .mapValues(Set::size)
                .suppress(Suppressed.untilWindowCloses(unbounded()));

        final KStream<String, String> countStream = aggregatedStream
                .toStream().map((k, v) -> KeyValue.pair(k.key() + "@" + k.window().start() + "->" + k.window().end(), v.toString()));

        countStream.to(TOPIC_OUT, Produced.with(Serdes.String(), Serdes.String()));

        return builder;
    }



}
