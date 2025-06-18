package backend.academy.bot.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public NewTopic deadLetterTopic(KafkaUpdateTopicPropertiesConfig kafkaTopicProperties) {
        return TopicBuilder.name(kafkaTopicProperties.name() + ".dlt")
                .partitions(kafkaTopicProperties.partitions())
                .replicas(kafkaTopicProperties.replicas())
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProducerPropertiesConfig kafkaProperties) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProperties.clientId());
        properties.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.acksMode());
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int)
                kafkaProperties.deliveryTimeout().toMillis());
        properties.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperties.lingerMs());
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProperties.batchSize());
        properties.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
