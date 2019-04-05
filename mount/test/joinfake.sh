docker-compose exec base /bin/bash

kafka-topics --create --zookeeper zk-1:2181 --topic ref_client --partitions 3 --replication-factor 1 --config cleanup.policy=compact
kafka-topics --create --zookeeper zk-1:2181 --topic iptv_event --partitions 3 --replication-factor 1 --config cleanup.policy=delete


kafka-topics --describe --zookeeper zk-1:2181 --topic client_ref


kafka-console-producer --broker-list kafka-1:9092 --topic ref_client --property "parse.key=true" --property "key.separator=:"
kafka-console-producer --broker-list kafka-1:9092 --topic iptv_event --property "parse.key=true" --property "key.separator=:"


kafka-console-consumer --bootstrap-server kafka-1:9092 --from-beginning --topic ref_client

kafka-console-consumer --bootstrap-server kafka-1:9092 --from-beginning --topic iptv_event

kafka-topics --zookeeper zk-1:2181 --alter --topic tpdelete --config retention.ms=0
docker logs -f kafka-1

docker-compose exec ksql-cli ksql http://ksql-server:8088



echo -e "5:5e\n6:6f\n7:7g" > /tmp/file-input.txt
cat /tmp/file-input.txt | kafka-console-producer --broker-list kafka-1:9092 --topic tpdelete --property "parse.key=true" --property "key.separator=:"

SET 'auto.offset.reset' = 'earliest';
print tpdelete

CREATE STREAM ks_iptv_event \
  (event_name VARCHAR, \
   id_client VARCHAR, \
   channel VARCHAR) \
  WITH (KAFKA_TOPIC='iptv_event', \
        VALUE_FORMAT='DELIMITED');



CREATE STREAM kt_ref_client \
   (id_client VARCHAR, \
    region VARCHAR) \
  WITH (KAFKA_TOPIC='ref_client', \
        VALUE_FORMAT='DELIMITED');

CREATE STREAM kt_ref_client_with_key \
  WITH (KAFKA_TOPIC='KT_REF_CLIENT_WITH_KEY', \
        PARTITIONS=3, \
        VALUE_FORMAT='DELIMITED')\
  AS SELECT id_client, region \
  FROM  kt_ref_client \
  PARTITION BY id_client ;

CREATE TABLE clienttable \
  (id_client VARCHAR, \
    region VARCHAR) \
  WITH (KAFKA_TOPIC='KT_REF_CLIENT_WITH_KEY', \
        VALUE_FORMAT='DELIMITED',\
        KEY = 'id_client');

CREATE STREAM result_ksql AS \
  SELECT ks.id_client AS ID , \
      kt.region, \
      ks.event_name, \
      ks.channel \
  FROM ks_iptv_event ks \
LEFT JOIN clienttable kt ON ks.id_client = kt.id_client;
