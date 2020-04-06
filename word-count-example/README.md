# word-count
Sample word count with Kafka, Spark and Cassandra

## Steps to run
1. If first time run, create the kafka topics `messages` with following commands
```
docker-compose exec kafka  \
kafka-topics --create --topic messages --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
```
2. If first time run, create the cassandra keyspace and table
- Download the cassandra client and connect to docker cassandra server
```
~/Downloads/apache-cassandra-3.11.6/bin/cqlsh 192.168.1.253 9042
```
- Run the CQL to create keyspace and table
```
CREATE KEYSPACE vocabulary
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
    };
USE vocabulary;
CREATE TABLE words (word text PRIMARY KEY, count int);
```
3. Run the spark job
- Build the maven project with ```mvn clean install```
- Run the java fat jar with following command
```
~/Downloads/spark-2.0.0-bin-hadoop2.7/bin/spark-submit \
  --class com.babar.WordCountingApp \
  --master local[2] \
  target/word-count-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```
4. Run data producer to send message to kafka
- Go to karka-producer directory and install dependency ```pip install -r requirements.txt```
- Run the producer with command:
```
python data-producer.py messages 192.168.1.253:9092
```