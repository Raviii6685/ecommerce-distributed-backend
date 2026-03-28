package zatribune.spring.ex_mongodb_docker.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica2")
    public DataSource replica2DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica1,
            @Qualifier("replica2DataSource") DataSource replica2) {

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
        private final java.util.concurrent.atomic.AtomicInteger counter = 
            new java.util.concurrent.atomic.AtomicInteger(0);

        @Override
        protected Object determineCurrentLookupKey() {
            if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                return "primary";
            }
            // Round robin between replicas
            int count = counter.getAndIncrement();
            return count % 2 == 0 ? "replica1" : "replica2";
        }
    };

    Map<Object, Object> sources = new HashMap<>();
    sources.put("primary", primary);
    sources.put("replica1", replica1);
    sources.put("replica2", replica2);

    routing.setTargetDataSources(sources);
    routing.setDefaultTargetDataSource(primary);
    routing.afterPropertiesSet();
    return new LazyConnectionDataSourceProxy(routing);
}
}