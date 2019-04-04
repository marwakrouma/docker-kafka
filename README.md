# Démarrage infra local

```
docker-compose -f docker-compose.yml up -d

docker-compose -f docker-compose.yml ps

Name                  Command            State              Ports
----------------------------------------------------------------------------------
base              /bin/sh                     Up      8083/tcp, 9092/tcp
kafka-1           /etc/confluent/docker/run   Up      9092/tcp
ksql-cli          /bin/sh                     Up
ksql-server       /etc/confluent/docker/run   Up      0.0.0.0:8088->8088/tcp
schema-registry   /etc/confluent/docker/run   Up      8081/tcp
zk-1              /etc/confluent/docker/run   Up      2181/tcp, 2888/tcp, 3888/tcp
```

# Connection dans le container base pour execution de commande kafka

```
docker-compose -f docker-compose.yml exec base bash
```

# Connection dans le container ksql-cli pour execution de commande ksql

```
docker-compose -f docker-compose.yml exec ksql-cli ksql http://ksql-server:8088

LIST topics;
```

# Démarrage infra local monitoring

```
docker-compose -f docker-compose-monitoring.yml up -d

docker-compose -f docker-compose-monitoring.yml ps

Name                      Command               State            Ports
---------------------------------------------------------------------------------------
cadvisor               /usr/bin/cadvisor -logtostderr   Up       0.0.0.0:8080->8080/tcp
control-center         /etc/confluent/docker/run        Up       0.0.0.0:9021->9021/tcp
grafana                /run.sh                          Up       0.0.0.0:3000->3000/tcp
kafka-1-jmx-exporter   /opt/entrypoint.sh               Up
monitoring             /opt/entrypoint.sh               Exit 7
prometheus             /bin/prometheus --config.f ...   Up       0.0.0.0:9090->9090/tcp
zk-1-jmx-exporter      /opt/entrypoint.sh               Up
```

Control center => http://localhost:9021/

Grafana => http://localhost:3000/

Prometheus => http://localhost:9090/

Cadvisor => http://localhost:8080/


Relancer la commande suivante si les dashboards ne sont pas présents :

```
docker-compose -f docker-compose-monitoring.yml up monitoring
```
