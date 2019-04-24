package fr.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Launcher implements CommandLineRunner {

    @Autowired
    List<Topology> topologies;

    Collection<KafkaStreams> kafkaStreams = new ArrayList<>();

    public static void main(final String[] args) {
        new SpringApplication(Launcher.class)
                .run(args)
                .registerShutdownHook();
    }

    public void run(String... args) {
        for (Topology topology : topologies) {
            topology.setSpecificAvroSerde(new SpecificAvroSerde<>());
            kafkaStreams.add(new KafkaStreams(topology.getTopology().build(), topology.getProperties()));
        }

        for (KafkaStreams ks : kafkaStreams) {
            ks.cleanUp();
            ks.start();
        }
    }
}
