package com.babar;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import scala.Tuple2;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WordCountingApp {

	@Inject
	@Named("kafka.host")
	String kafkaHost;
	
	@Inject
	@Named("kafka.port")
	String kafkaPort;
	
	@Inject
	@Named("kafka.topic")
	String kafkaTopic;
	
	@Inject
	@Named("cassandra.host")
	String cassandraHost;

	static class WordCountModule extends AbstractModule {
		@Override
		protected void configure() {
			try {
				Properties properties = new Properties();
				properties.load(getClass().getResourceAsStream("/application.properties"));
				Names.bindProperties(binder(), properties);
			} catch (IOException e) {
				log.error("Could not load config: ", e);
				System.exit(1);
			}
		}
	}

	public void process() throws InterruptedException {
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);

		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", kafkaHost + ":" + kafkaPort);
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", "wordCountConsumer");
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("enable.auto.commit", false);

		Collection<String> topics = Arrays.asList(kafkaTopic);

		SparkConf sparkConf = new SparkConf();
		sparkConf.setMaster("local[2]");
		sparkConf.setAppName("WordCountingApp");
		sparkConf.set("spark.cassandra.connection.host", cassandraHost);

		JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(1));

		JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(streamingContext,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

		JavaPairDStream<String, String> results = messages
				.mapToPair(record -> new Tuple2<>(record.key(), record.value()));

		JavaDStream<String> lines = results.map(tuple2 -> tuple2._2());

		JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split("\\s+")).iterator());

		JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1))
				.reduceByKey((i1, i2) -> i1 + i2);

		wordCounts.foreachRDD(javaRdd -> {
			Map<String, Integer> wordCountMap = javaRdd.collectAsMap();
			for (String key : wordCountMap.keySet()) {
				List<Word> wordList = Arrays.asList(new Word(key, wordCountMap.get(key)));
				JavaRDD<Word> rdd = streamingContext.sparkContext().parallelize(wordList);
				javaFunctions(rdd).writerBuilder("vocabulary", "words", mapToRow(Word.class)).saveToCassandra();
			}
		});

		streamingContext.start();
		streamingContext.awaitTermination();
	}

	public static void main(String[] args) throws InterruptedException {
		Injector injector = Guice.createInjector(new WordCountModule());
		injector.getInstance(WordCountingApp.class).process();
	}
}