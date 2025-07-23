package owleaf.quantumexitscan.Managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import owleaf.quantumexitscan.QuantumExitScan;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final QuantumExitScan plugin;
    private final ConfigManager config;
    private final UUIDManager uuidManager;

    public CommandManager(QuantumExitScan plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.uuidManager = plugin.getUUIDManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "radio":
                return handleRadio(sender, args);
            case "life":
                return handleLife(sender, args);
            case "reset":
                return handleReset(sender);
            case "list":
                return handleList(sender);
            case "warnings":
                return handleWarnings(sender, args);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender, "quantumexit.admin")) {
            return true;
        }

        config.reloadConfig();
        config.reloadWarningsConfig();
        uuidManager.reloadUUIDs();
        sender.sendMessage(ChatColor.GREEN + "Configuración recargada correctamente.");
        sendConfigInfo(sender);
        return true;
    }

    private boolean handleReset(CommandSender sender) {
        if (!hasPermission(sender, "quantumexit.admin")) {
            return true;
        }

        try {
            config.getConfig().set("detection-radius", 10);
            config.getConfig().set("low-life-threshold", 15);
            config.saveConfig();
            config.reloadConfig();

            sender.sendMessage(ChatColor.GREEN + "Configuración restablecida a valores por defecto.");
            sendConfigInfo(sender);
            return true;
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error al resetear la configuración.");
            plugin.getLogger().severe("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean handleRadio(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "quantumexit.radio")) {
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(formatUsage("/q radio <1-50>", "Cambia radio de detección"));
            return true;
        }

        try {
            int newRadius = Integer.parseInt(args[1]);
            if (newRadius < 1 || newRadius > 50) {
                sender.sendMessage(ChatColor.RED + "El radio debe estar entre 1 y 50 bloques.");
                return true;
            }

            try {
                config.setDetectionRadius(newRadius);
                sender.sendMessage(String.format("%sRadio cambiado a %s%d bloques%s",
                        ChatColor.GREEN, ChatColor.WHITE, newRadius, ChatColor.GREEN));
                return true;
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Error al guardar los cambios.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Debe ser un número entre 1 y 50.");
            return false;
        }
    }

    private boolean handleLife(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "quantumexit.life")) {
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(formatUsage("/q life <1-20>", "Cambia umbral de vida baja"));
            return true;
        }

        try {
            int newThreshold = Integer.parseInt(args[1]);
            if (newThreshold < 1 || newThreshold > 20) {
                sender.sendMessage(ChatColor.RED + "El umbral debe estar entre 1 y 20 puntos.");
                return true;
            }

            try {
                config.setLowLifeThreshold(newThreshold);
                sender.sendMessage(String.format("%sUmbral cambiado a %s%d puntos%s",
                        ChatColor.GREEN, ChatColor.WHITE, newThreshold, ChatColor.GREEN));
                return true;
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Error al guardar los cambios.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Debe ser un número entre 1 y 20.");
            return false;
        }
    }

    private boolean handleList(CommandSender sender) {
        if (!hasPermission(sender, "quantumexit.list")) {
            return true;
        }

        List<String> players = uuidManager.getAllowedPlayers();
        sender.sendMessage(ChatColor.GOLD + "=== Jugadores autorizados ===");

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No hay jugadores autorizados");
        } else {
            players.forEach(name ->
                    sender.sendMessage(String.format("%s- %s%s",
                            ChatColor.GRAY, ChatColor.WHITE, name)));
        }
        return true;
    }

    private boolean handleWarnings(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "quantumexit.warnings")) {
            return true;
        }

        List<Map<?, ?>> history = config.getWarningsConfig().getMapList("history");

        if (args.length > 1 && args[1].length() == 8) {
            showWarningDetails(sender, history, args[1]);
        } else {
            listWarnings(sender, history);
        }
        return true;
    }

    private void listWarnings(CommandSender sender, List<Map<?, ?>> history) {
        sender.sendMessage(ChatColor.GOLD + "=== Últimas Advertencias ===");

        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No hay advertencias registradas.");
            return;
        }

        int toShow = Math.min(5, history.size());
        for (int i = 0; i < toShow; i++) {
            Map<?, ?> warning = history.get(i);
            sender.sendMessage(String.format("%s[%s] %s%s - %s",
                    ChatColor.YELLOW,
                    warning.get("timestamp"),
                    ChatColor.WHITE,
                    warning.get("player"),
                    warning.get("reason")));
            sender.sendMessage(String.format("%sID: %s%s | %sUbicación: %s%s",
                    ChatColor.GRAY,
                    ChatColor.YELLOW,
                    warning.get("uuid").toString().substring(0, 8),
                    ChatColor.DARK_GRAY,
                    ChatColor.GRAY,
                    warning.get("location")));
        }

        if (history.size() > 5) {
            sender.sendMessage(ChatColor.GRAY + "Mostrando 5 de " + history.size() +
                    " advertencias. Usa /q warnings <id> para detalles.");
        }
    }

    private void showWarningDetails(CommandSender sender, List<Map<?, ?>> history, String warningId) {
        for (Map<?, ?> warning : history) {
            if (warning.get("uuid").toString().startsWith(warningId)) {
                sender.sendMessage(ChatColor.GOLD + "=== Detalles de Advertencia ===");
                sender.sendMessage(ChatColor.YELLOW + "Jugador: " + ChatColor.WHITE + warning.get("player"));
                sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + warning.get("uuid"));
                sender.sendMessage(ChatColor.YELLOW + "Motivo: " + ChatColor.WHITE + warning.get("reason"));
                sender.sendMessage(ChatColor.YELLOW + "Vida: " + ChatColor.WHITE + warning.get("health"));
                sender.sendMessage(ChatColor.YELLOW + "Hostiles: " + ChatColor.WHITE + warning.get("hostiles"));
                sender.sendMessage(ChatColor.YELLOW + "Ubicación: " + ChatColor.WHITE + warning.get("location"));
                sender.sendMessage(ChatColor.YELLOW + "Fecha: " + ChatColor.WHITE +
                        new Date((long) warning.get("timestamp") * 1000));
                return;
            }
        }
        sender.sendMessage(ChatColor.RED + "No se encontró ninguna advertencia con ese ID.");
    }

    private void sendConfigInfo(CommandSender sender) {
        sender.sendMessage(String.format("%s- %sRadio: %s%d bloques",
                ChatColor.GRAY, ChatColor.WHITE, ChatColor.WHITE, config.getDetectionRadius()));
        sender.sendMessage(String.format("%s- %sUmbral vida: %s%d puntos",
                ChatColor.GRAY, ChatColor.WHITE, ChatColor.WHITE, config.getLowLifeThreshold()));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos de QuantumExitScan ===");

        if (hasPermission(sender, "quantumexit.admin")) {
            sender.sendMessage(formatUsage("/q reload", "Recarga la configuración"));
            sender.sendMessage(formatUsage("/q reset", "Restablece configuración"));
        }

        if (hasPermission(sender, "quantumexit.radio")) {
            sender.sendMessage(formatUsage("/q radio <1-50>", "Cambia radio de detección"));
        }

        if (hasPermission(sender, "quantumexit.life")) {
            sender.sendMessage(formatUsage("/q life <1-20>", "Cambia umbral de vida"));
        }

        if (hasPermission(sender, "quantumexit.list")) {
            sender.sendMessage(formatUsage("/q list", "Muestra jugadores autorizados"));
        }

        if (hasPermission(sender, "quantumexit.warnings")) {
            sender.sendMessage(formatUsage("/q warnings", "Muestra advertencias recientes"));
            sender.sendMessage(formatUsage("/q warnings <id>", "Detalles de advertencia"));
        }
    }

    private String formatUsage(String command, String description) {
        return String.format("%s%s %s- %s",
                ChatColor.WHITE, command, ChatColor.GRAY, description);
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!uuidManager.isAllowed(player.getUniqueId()) || !player.hasPermission(permission)) {
                player.sendMessage(ChatColor.RED + "No tienes permiso para este comando.");
                return false;
            }
        } else if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores o consola.");
            return false;
        }
        return true;
    }
}