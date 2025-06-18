package backend.academy.scrapper;

import com.redis.testcontainers.RedisContainer;
import java.nio.file.Path;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.SneakyThrows;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public class IntegrationEnvironment {
    public static PostgreSQLContainer<?> POSTGRES;
    public static KafkaContainer KAFKA;
    public static RedisContainer REDIS;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("link-scrapper")
                .withUsername("postgres")
                .withPassword("postgres");

        POSTGRES.start();
        runMigrations(POSTGRES);

        KAFKA = new KafkaContainer("apache/kafka-native:3.8.0");
        KAFKA.start();

        //        REDIS = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
        REDIS = new RedisContainer("redis:6.2.6");
        REDIS.start();
    }

    @SneakyThrows
    private static void runMigrations(JdbcDatabaseContainer<?> container) {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(container.createConnection("")));

        Liquibase liquibase =
                new Liquibase("master.xml", new DirectoryResourceAccessor(Path.of("../migrations")), database);
        liquibase.update();
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Kafka

        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", KAFKA::getBootstrapServers);
        //        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        //        registry.add("spring.kafka.producer.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("app.use-queue", () -> true);

        // Redis
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        //        registry.add("database.accessor", () -> "jdbc");
    }
}
