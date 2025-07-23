package owleaf.quantumexitscan;

import org.bukkit.plugin.java.JavaPlugin;
import owleaf.quantumexitscan.Managers.CommandManager;
import owleaf.quantumexitscan.Managers.ConfigManager;
import owleaf.quantumexitscan.Managers.UUIDManager;
import owleaf.quantumexitscan.PlayerWarn.PlayerExitDetect;
import owleaf.quantumexitscan.PlayerWarn.PlayerWarn;

public class QuantumExitScan extends JavaPlugin {

    private static JavaPlugin instance;
    private ConfigManager configManager;
    private UUIDManager uuidManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        configManager = new ConfigManager(this);
        configManager.setupConfig();

        uuidManager = new UUIDManager(this);

        getServer().getPluginManager().registerEvents(new PlayerExitDetect(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWarn(this), this);

        this.getCommand("q").setExecutor(new CommandManager(this));

        getLogger().info("§aQuantumExitScan activado!");
        getLogger().info("§7- Radio de detección: §f" + configManager.getDetectionRadius() + " bloques");
        getLogger().info("§7- Umbral de vida baja: §f" + configManager.getLowLifeThreshold() + " puntos");
        getLogger().info("§7- UUIDs permitidos: §f" + uuidManager.getAllowedUUIDs().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("§cQuantumExitScan desactivado");
    }

    public static JavaPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public UUIDManager getUUIDManager() {
        return uuidManager;
    }
}