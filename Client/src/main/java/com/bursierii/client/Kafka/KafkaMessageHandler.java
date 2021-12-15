package com.bursierii.client.Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaMessageHandler {

    void handleMessage(ConsumerRecord<String, String> record);

}
