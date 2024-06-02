package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.handlers.ChestLockHandler;
import de.buuddyyy.birnaloniasystem.handlers.PlayerHandler;
import de.buuddyyy.birnaloniasystem.managers.PlayerManager;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChestLockCommand implements CommandExecutor, TabExecutor {

    private final BirnaloniaSystemPlugin plugin;

    public ChestLockCommand(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player))
            return false;
        final Player p = (Player) commandSender;
        if (args.length == 2) {
            final PlayerHandler playerHandler = this.plugin.getPlayerHandler();
            final PlayerManager playerManager = playerHandler.getPlayerManager();
            if ("trust".equalsIgnoreCase(args[0])) {
                String playerName = args[1];
                PlayerEntity targetEntity;
                if ((targetEntity = playerManager.getPlayerByName(playerName)) == null) {
                    p.sendMessage(this.plugin.getPrefix() + "§cDer Spieler §e" + playerName + " §cexistiert nicht!");
                    return false;
                }
                if (targetEntity.getPlayerUuid().equals(p.getUniqueId())) {
                    p.sendMessage(this.plugin.getPrefix() + "§cDu kannst nicht mit dir selbst interagieren!");
                    return false;
                }
                playerName = targetEntity.getPlayerName();
                ChestLockHandler.CHEST_LOCK_TRUST_MAP.put(p.getUniqueId(), targetEntity);
                ChestLockHandler.CHEST_LOCK_ACTION_MAP.put(p.getUniqueId(),
                        ChestLockHandler.EnumChestLockAction.TRUST_PLAYER);
                p.sendMessage(this.plugin.getPrefix() + "§7Klicke nun §erechtsklick §7auf den Block, auf den §e"
                        + playerName + " §7Zugriff haben soll.");
                return true;
            } else if ("untrust".equalsIgnoreCase(args[0])) {
                String playerName = args[1];
                PlayerEntity targetEntity;
                if ((targetEntity = playerManager.getPlayerByName(playerName)) == null) {
                    p.sendMessage(this.plugin.getPrefix() + "§cDer Spieler §e" + playerName + " §cexistiert nicht!");
                    return false;
                }
                if (targetEntity.getPlayerUuid().equals(p.getUniqueId())) {
                    p.sendMessage(this.plugin.getPrefix() + "§cDu kannst nicht mit dir selbst interagieren!");
                    return false;
                }
                playerName = targetEntity.getPlayerName();
                ChestLockHandler.CHEST_LOCK_TRUST_MAP.put(p.getUniqueId(), targetEntity);
                ChestLockHandler.CHEST_LOCK_ACTION_MAP.put(p.getUniqueId(),
                        ChestLockHandler.EnumChestLockAction.UNTRUST_PLAYER);
                p.sendMessage(this.plugin.getPrefix() + "§7Klicke nun §erechtsklick §7auf den Block, auf den §e"
                        + playerName + " §7keinen mehr Zugriff haben soll.");
                return true;
            }
        } else if (args.length == 1) {
            if ("lock".equalsIgnoreCase(args[0])) {
                ChestLockHandler.CHEST_LOCK_ACTION_MAP.put(p.getUniqueId(),
                        ChestLockHandler.EnumChestLockAction.CREATE);
                p.sendMessage(this.plugin.getPrefix() + "§7Klicke nun §erechtsklick §7auf den Block, den du " +
                        "§asichern §7möchtest.");
                return true;
            } else if ("unlock".equalsIgnoreCase(args[0])) {
                ChestLockHandler.CHEST_LOCK_ACTION_MAP.put(p.getUniqueId(),
                        ChestLockHandler.EnumChestLockAction.REMOVE);
                p.sendMessage(this.plugin.getPrefix() + "§7Klicke nun §erechtsklick §7auf den Block, den du " +
                        "§centsichern §7möchtest.");
                return true;
            } else if ("info".equalsIgnoreCase(args[0])) {
                ChestLockHandler.CHEST_LOCK_ACTION_MAP.put(p.getUniqueId(),
                        ChestLockHandler.EnumChestLockAction.INFO);
                p.sendMessage(this.plugin.getPrefix() + "§7Klicke nun §erechtsklick §7auf den Block, von der " +
                        "du die Informationen sehen möchtest.");
                return true;
            } else if ("toggle".equalsIgnoreCase(args[0])) {
                final PlayerHandler ph = this.plugin.getPlayerHandler();
                boolean newValue = !ph.isAutoChestLockEnabled(p);
                ph.setAutoChestLock(p, newValue);
                p.sendMessage(this.plugin.getPrefix() + "§7Auto ChestLock: "
                        + (newValue ? "§aaktiviert" : "§cdeaktiviert"));
                return true;
            }
        }
        p.sendMessage(this.plugin.getPrefix() + "§eAlle ChestLock-Befehle:",
                " §7/chestlock lock",
                " §7/chestlock unlock",
                " §7/chestlock trust <Spieler>",
                " §7/chestlock untrust <Spieler>",
                " §7/chestlock info",
                " §7/chestlock toggle",
                "§cEntwickelt von §6buuddyyy");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            return Arrays.asList("lock", "unlock", "trust", "untrust", "info", "toggle");
        } else if (args.length == 1) {
            if ("trust".equalsIgnoreCase(args[0]) || "untrust".equalsIgnoreCase(args[0])) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
