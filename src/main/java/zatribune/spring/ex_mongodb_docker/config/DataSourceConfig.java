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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    // Saari 3 nodes — koi bhi Leader/Replica ban sakta hai
    private static final String[] ALL_NODES = {
        "http://10.128.0.2:8008",
        "http://10.128.0.3:8008",
        "http://10.128.0.4:8008"
    };

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
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            protected Object determineCurrentLookupKey() {
                if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    System.out.println(">>> ROUTING TO: primary");
                    return "primary";
                }

                // Dynamic replica check — saari nodes check karo
                String[] replicaKeys = {"replica1", "replica2"};
                String[] replicaUrls = {
                    "http://10.128.0.2:8008/replica",
                    "http://10.128.0.3:8008/replica",
                    "http://10.128.0.4:8008/replica"
                };

                // Round robin start point
                int start = counter.getAndIncrement() % 3;

                // Saari nodes check karo — jo /replica pe 200 de woh use karo
                for (int i = 0; i < 3; i++) {
                    int idx = (start + i) % 3;
                    try {
                        HttpURLConnection conn = (HttpURLConnection)
                            new URL(replicaUrls[idx]).openConnection();
                        conn.setConnectTimeout(300);
                        conn.setReadTimeout(300);
                        int status = conn.getResponseCode();
                        conn.disconnect();

                        if (status == 200) {
                            String key = idx == 0 ? "replica1" : 
                                        idx == 1 ? "replica2" : "replica3";
                            System.out.println(">>> ROUTING TO: " + key + " (node " + idx + ")");
                            return key;
                        }
                    } catch (Exception e) {
                        System.out.println(">>> Node " + idx + " unreachable, trying next...");
                    }
                }

                // Sab fail — primary pe fallback
                System.out.println(">>> All replicas down! Fallback to primary");
                return "primary";
            }
        };

        Map<Object, Object> sources = new HashMap<>();
        sources.put("primary", primary);
        sources.put("replica1", replica1);
        sources.put("replica2", replica2);
        sources.put("replica3", replica2); // 3rd node bhi replica2 pool use karega

        routing.setTargetDataSources(sources);
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();
        return new LazyConnectionDataSourceProxy(routing);
    }
}
