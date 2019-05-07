package fr.test.topology;

import fr.test.Topology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;



import fr.test.model.Client;
import fr.test.model.Iptv;
import fr.test.model.ClientIptv;

/**
 * Topology qui fait le count sur une fenetre de temps de 1 minute.
 * L'utilisation de l'operateur suppress permet d'émettre uniquement le dernier evenement pour chaque clef de la fenetre de temps
 */
@Component
public class EventEnrich extends Topology {

    static final String IPTV_TOPIC = "iptv_event_avro";
    static final String Client_TOPIC = "ref_client_avro";

    static final String Client_STORE = "clientstoreenrich";
    static final String ENRICHED_IPTV_TOPIC = "client_iptv";


    public StreamsBuilder getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        // Get the stream of iptv events
        final KStream<String, Iptv> iptvStream = builder.stream(IPTV_TOPIC, Consumed.with(Serdes.String(), getSpecificAvroSerde()));

        // Create a global table for customers. The data from this global table
        // will be fully replicated on each instance of this application.
        final GlobalKTable<String, Client>
                clients =
                builder.globalTable(Client_TOPIC, Materialized.<String, Client, KeyValueStore<Bytes, byte[]>>as(Client_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(getSpecificAvroSerde()));

        // Join the iptv stream to the clients global table. As this is global table
        // we can use a non-key based join with out needing to repartition the input stream
        final KStream<String, ClientIptv> clientiptvStream = iptvStream.join(clients,
                (mac, iptvevent ) -> iptvevent.getMac(),
                (iptv, client) -> new ClientIptv(client.getMac(),iptv.getMac(), client.getName(), client.getCity(),
                        iptv.getGroupid(),iptv.getLkp(), iptv.getLzd(), iptv.getZip()));
        // write the enriched order to the enriched-order topic
        clientiptvStream.to(ENRICHED_IPTV_TOPIC, Produced.with(Serdes.String(), getSpecificAvroSerde()));
        return builder;
    }
}
