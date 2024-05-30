package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    private final BirnaloniaSystemPlugin plugin;

    public TpaCommand(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        final Player p = (Player) commandSender;
        if (args.length != 1) {
            p.sendMessage(this.plugin.getPrefix() + "§cBitte verwende: §e/tpa <Spieler>");
            return true;
        }
        final String playerName = args[0];
        Player toPlayer;
        if ((toPlayer = Bukkit.getPlayer(playerName)) == null) {
            p.sendMessage(this.plugin.getPrefix() + "§cDieser Spieler ist nicht online!");
            return true;
        }
        if (p.equals(toPlayer)) {
            p.sendMessage(this.plugin.getPrefix() + "§cDu kannst nicht mit dir selbst interagieren!");
            return true;
        }
        this.plugin.getTeleportHandler().sendTeleportRequest(p, toPlayer);
        return true;
    }

}
