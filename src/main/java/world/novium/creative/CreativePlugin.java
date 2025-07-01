package world.novium.creative;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.triumphteam.gui.TriumphGui;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import world.novium.creative.backend.BackendServer;
import world.novium.creative.common.StartupHook;
import world.novium.creative.database.Database;
import world.novium.creative.utils.GuiceModule;
import world.novium.creative.utils.ServiceRegistry;

import java.util.Set;

@Getter
@Accessors(fluent = true)
public class CreativePlugin extends JavaPlugin {
    private Injector injector;
    private ServiceRegistry registry;

    private BackendServer server;
    private Thread serverThread;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        CommandAPI.onEnable();

        TriumphGui.init(this);

        var databaseConfig = getConfig().getConfigurationSection("database");

        if (databaseConfig == null || !databaseConfig.getBoolean("enabled", false)) {
            getLogger().severe("Database is not enabled in the config. Please enable it to use the plugin.");
            return;
        }

        Database database = new Database();

        database.connect(databaseConfig);

        injector = Guice.createInjector(new GuiceModule(this));
        this.registry = new ServiceRegistry(this, getClassLoader(), injector);

        Set<StartupHook> startupHooks = injector.getInstance(
                Key.get(
                        new TypeLiteral<>() {}
                )
        );
        for (StartupHook hook : startupHooks) {
            hook.onStartup();
        }

        Bukkit.getServicesManager().register(ServiceRegistry.class, registry, this, ServicePriority.Normal);

        this.registry.registerAllListeners();
        this.registry.registerAllCommands();
        
        var backendConfig = getConfig().getConfigurationSection("backend");
        boolean backendEnabled = backendConfig != null && backendConfig.getBoolean("enabled", false);

        if (backendEnabled) {
            String authToken = backendConfig.getString("authToken", "auth-token");
            if (authToken.isEmpty()) {
                getLogger().severe("Backend server is enabled but no auth token is provided in the config.");
                return;
            }

            int port = backendConfig.getInt("port", 8080);

            server = new BackendServer(port, authToken, this);
            serverThread = new Thread(server);

            serverThread.start();

            getLogger().info("Backend server started on port " + port + " with auth token: " + authToken);
        }

        getLogger().info("CreativePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        if (server != null) {
            server.stop();
            if (serverThread != null && serverThread.isAlive()) {
                try {
                    serverThread.join();
                } catch (InterruptedException e) {
                    getLogger().severe("Failed to stop the backend server thread: " + e.getMessage());
                }
            }
        }
        // Plugin shutdown logic
        getLogger().info("CreativePlugin has been disabled!");
    }
}
