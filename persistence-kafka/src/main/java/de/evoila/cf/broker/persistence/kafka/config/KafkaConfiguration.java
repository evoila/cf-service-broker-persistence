package de.evoila.cf.broker.persistence.kafka.config;


import de.evoila.cf.broker.persistence.kafka.model.JobMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * @author Michael Hahn
 */
@EnableKafka
@Configuration
public class KafkaConfiguration {

    private KafkaProperties kafkaProperties;

    private String topicName = "backup-job";

    public KafkaConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, JobMessage> jobMessageConsumerFactory() {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();

        config.put(ConsumerConfig.GROUP_ID_CONFIG, "jobMessage_json2");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        //handle deserialization errors
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        //config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        //config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JobMessage.class);
        // only read new messages

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobMessage> jobMessageKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobMessageConsumerFactory());
        return factory;
    }

    @Bean
    public NewTopic backupJob() {
        return TopicBuilder.name(topicName)
                .build();
    }
}
