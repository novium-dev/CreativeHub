package world.novium.creative.common.managers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import world.novium.creative.common.Settings;
import world.novium.creative.common.StartupHook;

import java.io.File;

@Getter
public class SettingsManager implements StartupHook {
    private final File dataFolder;

    public SettingsManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    private Settings settings;

    @Override
    public void onStartup() {
        File settingsFile = new File(dataFolder, "settings.yml");
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settingsFile);

        if (!config.contains("maintenance-mode")) {
            config.set("maintenance-mode", false);
        }

        settings = new Settings(config.getBoolean("maintenance-mode"));
    }

    public void update(Settings settings) {
        this.settings = settings;
        saveSettings();
    }

    private void saveSettings() {
        File settingsFile = new File(dataFolder, "settings.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(settingsFile);

        config.set("maintenance-mode", settings.isMaintenanceMode());

        try {
            config.save(settingsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
