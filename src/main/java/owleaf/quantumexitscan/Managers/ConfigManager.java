package owleaf.quantumexitscan.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration warningsConfig;
    private File warningsFile;

    private int detectionRadius;
    private int lowLifeThreshold;
    private boolean trackEffects;
    private boolean trackEnvironmental;
    private boolean trackFallDamage;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.warningsFile = new File(plugin.getDataFolder(), "warnings.yml");
    }

    public void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!warningsFile.exists()) {
            plugin.saveResource("warnings.yml", false);
        }
        reloadConfig();
        reloadWarningsConfig();
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        detectionRadius = config.getInt("detection-radius", 10);
        lowLifeThreshold = config.getInt("low-life-threshold", 15);
        trackEffects = config.getBoolean("tracking.effects", true);
        trackEnvironmental = config.getBoolean("tracking.environmental", true);
        trackFallDamage = config.getBoolean("tracking.fall-damage", true);
        validateConfigValues();
    }

    public void reloadWarningsConfig() {
        warningsConfig = YamlConfiguration.loadConfiguration(warningsFile);
    }

    private void validateConfigValues() {
        if (detectionRadius < 1 || detectionRadius > 100) {
            detectionRadius = 10;
        }
        if (lowLifeThreshold < 1 || lowLifeThreshold > 20) {
            lowLifeThreshold = 15;
        }
    }

    public List<String> getNegativeEffects() {
        return config.getStringList("negative-effects");
    }

    public int getWarningTrackingEntitiesThreshold() {
        return config.getInt("warnings.conditions.tracking-entities", 1);
    }

    public boolean isTrackEffects() { return trackEffects; }
    public boolean isTrackEnvironmental() { return trackEnvironmental; }
    public boolean isTrackFallDamage() { return trackFallDamage; }
    public int getDetectionRadius() { return detectionRadius; }
    public int getLowLifeThreshold() { return lowLifeThreshold; }
    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getWarningsConfig() { return warningsConfig; }
    public File getWarningsFile() { return warningsFile; }

    public void setDetectionRadius(int radius) throws IOException {
        this.detectionRadius = radius;
        config.set("detection-radius", radius);
        saveConfig();
    }

    public void setLowLifeThreshold(int threshold) throws IOException {
        this.lowLifeThreshold = threshold;
        config.set("low-life-threshold", threshold);
        saveConfig();
    }

    public void saveConfig() throws IOException {
        config.save(configFile);
    }

    public void saveWarningsConfig() throws IOException {
        warningsConfig.save(warningsFile);
    }
}