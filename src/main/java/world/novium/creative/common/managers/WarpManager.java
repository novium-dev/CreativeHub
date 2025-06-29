package world.novium.creative.common.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import world.novium.creative.common.StartupHook;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@RequiredArgsConstructor
@Slf4j
public class WarpManager implements StartupHook {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, Warp> warps = new HashMap<>();

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void onStartup() {
        loadWarps();
    }

    private void loadWarps() {
        ConfigurationSection section = config.getConfigurationSection("warps");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            Location loc = section.getLocation(key + ".location");
            String nameStr = section.getString(key + ".name");
            if (loc != null && nameStr != null) {
                warps.put(key.toLowerCase(), new Warp(nameStr, loc));
            }
        }
    }

    public void saveWarp(String key, Location location, String displayName) {
        key = key.toLowerCase();
        warps.put(key, new Warp(displayName, location));
        config.set("warps." + key + ".location", location);
        config.set("warps." + key + ".name", displayName);
        saveFile();
    }

    public Optional<Warp> getWarp(String key) {
        return Optional.ofNullable(warps.get(key.toLowerCase()));
    }

    public boolean deleteWarp(String key) {
        key = key.toLowerCase();
        if (!warps.containsKey(key)) return false;

        warps.remove(key);
        config.set("warps." + key, null);
        saveFile();
        return true;
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Failed to save warps.yml: {}", e.getMessage());
        }
    }

    public Set<String> getWarpNames() {
        return new HashSet<>(warps.keySet());
    }

    public Set<Warp> getWarps() {
        return new HashSet<>(warps.values());
    }

    public record Warp(String name, Location location) {
    }
}
