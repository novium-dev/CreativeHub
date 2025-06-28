package world.novium.creative.database;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.mariadb.databases.MariaDb;
import de.chojo.sadu.mariadb.mapper.MariaDbMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Getter()
@Slf4j
public class Database {
    private HikariDataSource datastore;

    public void connect(
            @NotNull ConfigurationSection configSection
    ) {
        String username = configSection.getString("username", "root");
        String password = configSection.getString("password", "password");
        int maxPoolSize = configSection.getInt("max-pool-size", 10);
        int port = configSection.getInt("port", 3306);
        String host = configSection.getString("host", "localhost");
        String database = configSection.getString("database", "novium");


        if (host.isEmpty()) {
            throw new IllegalArgumentException("Database URI cannot be null or empty");
        }

        datastore = DataSourceCreator.create(MariaDb.get()).configure(config ->
                config.host(host)
                .port(port)
                .database(database)
                .user(username)
                .password(password)
        )
                .create()
                .withMaximumPoolSize(maxPoolSize)
                .withMinimumIdle(1)
                .build();

        configureDefaultQuery();

        log.info("Connected to database");
    }

    private void configureDefaultQuery() {
        QueryConfiguration config = QueryConfiguration.builder(datastore)
                .setExceptionHandler(err -> log.warn(err.getMessage()))
                .setThrowExceptions(true)
                .setAtomic(true)
                .setRowMapperRegistry(new RowMapperRegistry().register(MariaDbMapper.getDefaultMapper()))
                .build();
        QueryConfiguration.setDefault(config);
    }

}
