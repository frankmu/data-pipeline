# New York Taxi tips
Real time stock analyzer with Kafka, Spark and Redis

## Steps to run
1. If first time run, create the kafka topics `taxirides` `taxifares` with following commands
```
docker-compose exec kafka kafka-topics \
  --create --zookeeper zookeeper:2181 --replication-factor 1 \
  --partitions 1 --topic taxirides
docker-compose exec kafka kafka-topics \
  --create --zookeeper zookeeper:2181 --replication-factor 1 \
  --partitions 1 --topic taxifares
```
2. Prepare the data set
- Create a new folder for the data set
- From this link: https://training.ververica.com/setup/taxiData.html download the 2 data sets:
```
wget http://training.ververica.com/trainingData/nycTaxiRides.gz
wget http://training.ververica.com/trainingData/nycTaxiFares.gz
```
- Unzip the file
3. Download kafka client and we will use `kafka-console-producer.sh` to produce the data
4. Start Spark job
- Go to spark directory and download the spark client
- Run the command with spark client and local ip address
- Notice we need to increase the memory option otherwise it will OOM:(
```
~/Downloads/spark-2.4.5-bin-hadoop2.7/bin/spark-submit \
  --executor-memory 4g --driver-memory 4g \
  --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 \
  ny-taxi.py taxirides taxifares 192.168.1.253:9092
``` 
5. Produce the data through `kafka-console-producer.sh`
From data set folder, run the following 2 commands:
```
~/Downloads/kafka_2.13-2.4.1/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic taxirides < nycTaxiRides
~/Downloads/kafka_2.13-2.4.1/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic taxifares < nycTaxiFares
```
6. Sample output from console of the spark job
```
+------------------------------------------+------------------+
|window                                    |avgtip            |
+------------------------------------------+------------------+
|[2013-01-03 20:30:00, 2013-01-03 21:00:00]|1.2527241089582801|
|[2013-01-03 21:20:00, 2013-01-03 21:50:00]|1.2713917461197186|
|[2013-01-01 23:30:00, 2013-01-02 00:00:00]|1.1698616274615796|
|[2013-01-02 10:00:00, 2013-01-02 10:30:00]|1.0677769772057217|
|[2013-01-02 01:50:00, 2013-01-02 02:20:00]|1.0428331554608354|
|[2013-01-04 01:30:00, 2013-01-04 02:00:00]|1.2238500750272676|
|[2013-01-03 11:40:00, 2013-01-03 12:10:00]|0.9314146685442946|
|[2013-01-03 08:30:00, 2013-01-03 09:00:00]|1.220413666380389 |
|[2013-01-02 18:40:00, 2013-01-02 19:10:00]|1.1426442778066983|
|[2013-01-03 04:00:00, 2013-01-03 04:30:00]|1.0277344880929327|
|[2013-01-04 03:30:00, 2013-01-04 04:00:00]|1.0331869997974048|
|[2013-01-01 16:40:00, 2013-01-01 17:10:00]|1.0411509137855162|
|[2013-01-01 06:40:00, 2013-01-01 07:10:00]|1.090235661071464 |
|[2013-01-01 21:10:00, 2013-01-01 21:40:00]|1.1263541507933028|
|[2013-01-01 05:10:00, 2013-01-01 05:40:00]|1.1480996011837366|
|[2013-01-01 08:00:00, 2013-01-01 08:30:00]|1.099641040880252 |
|[2013-01-02 02:50:00, 2013-01-02 03:20:00]|0.7626083022158338|
|[2013-01-01 06:50:00, 2013-01-01 07:20:00]|1.0771902812295435|
|[2013-01-04 14:30:00, 2013-01-04 15:00:00]|0.9796316036978571|
|[2013-01-01 02:50:00, 2013-01-01 03:20:00]|1.1612003100450619|
+------------------------------------------+------------------+
only showing top 20 rows
```