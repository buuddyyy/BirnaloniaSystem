package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetSpawnCommand implements CommandExecutor {

    @Getter private final BirnaloniaSystemPlugin plugin;

    public SetSpawnCommand(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        final Player p = (Player) commandSender;
        if (!p.isOp()) {
            p.sendMessage(this.plugin.getPrefix() + "§cDu hast keine Rechte für diesen Befehl!");
            return true;
        }
        this.plugin.getTeleportHandler().setSpawnLocation(p.getLocation());
        p.sendMessage(this.plugin.getPrefix() + "§eSpawn-Location §7wurde gesetzt.");
        return true;
    }

}
