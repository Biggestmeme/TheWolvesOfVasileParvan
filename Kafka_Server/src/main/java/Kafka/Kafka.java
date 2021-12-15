package Kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import Config.Config;

public class Kafka {
    private String address;
    private String group_id;

    public Kafka(String address, String group_id) {
        this.address = address;
        this.group_id = group_id;
    }

    public void sendMessage(String TOPIC,String object_key,String message) {
        // Check arguments length value


        // create instance for properties to access producer configs
        Properties props = new Properties();

        //Assign localhost id
        props.put("bootstrap.servers", this.address);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer
                <String, String>(props);

        ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC,object_key,message);
        producer.send(record);
        producer.flush();
        producer.close();

    }

    public void createConsumer(KafkaMessageHandler handler, String[] topics) {
        Properties props = new Properties();
        props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.address);
        props.setProperty("group.id", this.group_id);
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topics));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records)
                handler.handleMessage(record);


        }
    }

    public void createTopic(String kafka_properties,String topic,int numPartitions,short replicationFactor) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(new File(kafka_properties)));

        AdminClient adminClient = AdminClient.create(properties);
        NewTopic newTopic = new NewTopic(topic, numPartitions, replicationFactor); //new NewTopic(topicName, numPartitions, replicationFactor)

        List<NewTopic> newTopics = new ArrayList<NewTopic>();
        newTopics.add(newTopic);

        adminClient.createTopics(newTopics);
        adminClient.close();
    }

    public boolean checkTopicExistence(String topic) {
        return this.getTopics(false).contains(topic);
    }

    private ArrayList<String> getTopics(boolean withConsumers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_ADDRESS);
        try (AdminClient client = AdminClient.create(props)) {
            ListTopicsOptions options = new ListTopicsOptions();
            options.listInternal(withConsumers); // includes internal topics such as __consumer_offsets
            ListTopicsResult topics = client.listTopics(options);
            return new ArrayList<String>(topics.names().get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}


