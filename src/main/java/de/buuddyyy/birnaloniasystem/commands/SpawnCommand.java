package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    @Getter private BirnaloniaSystemPlugin plugin;

    public SpawnCommand(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        final Player p = (Player) commandSender;
        this.plugin.getTeleportHandler().teleportPlayerToSpawn(p);
        return true;
    }

}
