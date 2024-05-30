package de.buuddyyy.birnaloniasystem.commands;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.handlers.TeleportHandler;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeCommand implements CommandExecutor, TabExecutor {

    @Getter private final BirnaloniaSystemPlugin plugin;

    public HomeCommand(BirnaloniaSystemPlugin plugin) {
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
        if (!tph.existsPlayerHome(p, homeName)) {
            p.sendMessage(this.plugin.getPrefix() + "§cDu hast kein Home mit dem Namen!");
            return true;
        }
        tph.teleportPlayerToHome(p, homeName);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }

}
