# stock-analyzer
Real time stock analyzer with Kafka, Spark and Redis

## Diagram

![alt text](https://github.com/frankmu/data-pipeline/blob/master/stock-analyzer-example/doc/assets/diagram.png)

## Steps to run
1. If first time run, create the kafka topics `stock-analyzer` `average-stock-price` with following commands
```
docker-compose exec kafka  \
kafka-topics --create --topic stock-analyzer --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

docker-compose exec kafka  \
kafka-topics --create --topic average-stock-price --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
```
2. Start Spark job
- Go to spark directory and download the spark client
- Run the command with spark client and local ip address
```
~/Downloads/spark-2.0.0-bin-hadoop2.7/bin/spark-submit --jars spark-streaming-kafka-0-8-assembly_2.11-2.0.0.jar stream-processing.py stock-analyzer average-stock-price 192.168.1.253:9092
``` 
3. Start Redis job
- Go to Redis directory and install dependency ```pip install -r requirements.txt```
- Run the command to start redis consumer job with local ip address
```
python redis-publisher.py average-stock-price 192.168.1.253:9092 average-stock-price 192.168.1.253 6379
```
4. Start Nodejs server
- Go to nodejs directory and install dependency ```npm install```
- Run the command to start Nodejs server with local ip address
```
node index.js --port=3000 --redis_host=192.168.1.253 --redis_port=6379 --subscribe_topic=average-stock-price
```
5. Start Producer job
- Go to karka-producer directory and install dependency ```pip install -r requirements.txt```
- `simple-data-producer` will produce data every second and can be started with command
```
python simple-data-producer.py AAPL stock-analyzer 192.168.1.253:9092
```
- `fast-data-producer` will produce data with high speed and can be started with command
```
python fast-data-producer.py stock-analyzer 192.168.1.253:9092
```