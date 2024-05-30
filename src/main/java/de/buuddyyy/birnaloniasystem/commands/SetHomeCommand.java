package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.handlers.TeleportHandler;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class SetHomeCommand implements CommandExecutor {

    @Getter private final BirnaloniaSystemPlugin plugin;

    public SetHomeCommand(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        final Player p = (Player) commandSender;
        final TeleportHandler tph = this.plugin.getTeleportHandler();
        String homeName;
        if (args.length == 0) {
            homeName = "home";
        } else {
            homeName = args[0];
        }
        homeName = homeName.toLowerCase();
        tph.setPlayerHome(p, homeName, p.getLocation());
        p.sendMessage(this.plugin.getPrefix() + "Home §e\"" + homeName + "\"§7 wurde gesetzt.");
        return true;
    }

}
