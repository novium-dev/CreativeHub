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
import org.bukkit.plugin.java.JavaPlugin;
import world.novium.creative.common.StartupHook;
import world.novium.creative.database.Database;
import world.novium.creative.utils.GuiceModule;
import world.novium.creative.utils.ServiceRegistry;

import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class CreativePlugin extends JavaPlugin {
    private Injector injector;

    private ServiceRegistry registry;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

        CommandAPI.onEnable();

        TriumphGui.init(this);

        Bukkit.getServicesManager().register(ServiceRegistry.class, registry, this, org.bukkit.plugin.ServicePriority.Normal);

        this.registry.registerAllListeners();
        this.registry.registerAllCommands();

        getLogger().info("CreativePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        // Plugin shutdown logic
        getLogger().info("CreativePlugin has been disabled!");
    }
}
