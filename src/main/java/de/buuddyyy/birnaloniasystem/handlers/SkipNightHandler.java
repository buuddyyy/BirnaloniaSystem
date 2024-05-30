package de.buuddyyy.birnaloniasystem.handlers;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SkipNightHandler {

    private static final Map<World, List<Player>> PLAYERS_SLEEPING = Maps.newHashMap();
    private static final Map<World, BukkitTask> PLAYERS_SLEEPING_RUNNABLES = Maps.newHashMap();

    private static final List<String> FLAT_BAD_JOKES = Arrays.asList(
            "Guten Morgen! Zeit um wortwörtlich um den Block zu gehen!",
            "Guten Morgen! Zeit, den Tag mit einem guten Block Kaffee zu starten!",
            "Hey, aufstehen! Heute steht auf dem Programm: ein Block nach dem anderen!",
            "Guten Morgen! Lasst uns heute einige Meilensteine - oder besser gesagt Blöcke - erreichen!",
            "Wakey wakey! Zeit, den Tag Stein für Stein aufzubauen!",
            "Guten Morgen! Heute bauen wir die besten Erinnerungen - Block für Block!",
            "Aufstehen! Zeit, den Tag mit einem Blockbuster zu starten!",
            "Guten Morgen! Lasst uns heute alles in vollen Blöcken genießen!",
            "Morgen! Lasst uns die Welt Block für Block erobern!",
            "Aufwachen! Lasst uns den Tag blockweise genießen!",
            "Guten Morgen! Zeit, das Leben einen Block nach dem anderen anzugehen!"
    );

    private final BirnaloniaSystemPlugin plugin;

    public SkipNightHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleBedEnter(Player player) {
        final World w = player.getWorld();
        if (!isNight(w))
            return;
        long totalPlayers = w.getPlayers().size();
        if (totalPlayers == 1)
            return;
        final List<Player> sleepingPlayers = PLAYERS_SLEEPING.getOrDefault(w, new ArrayList<>());
        if (!sleepingPlayers.contains(player)) {
            sleepingPlayers.add(player);
            PLAYERS_SLEEPING.put(w, sleepingPlayers);
            int sleepingCount = sleepingPlayers.size();
            int requiredPlayers = (int) Math.ceil(totalPlayers/2.0);
            w.getPlayers().forEach(pAll -> pAll.sendMessage(this.plugin.getPrefix() + "§e" + player.getName()
                    + " §7schläft... zZzZ §8[§e" + sleepingCount + "§7/" + requiredPlayers + "§8]"));
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> checkSleepingPlayers(w), 5L);
    }

    public void handleBedLeave(Player player) {
        final World w = player.getWorld();
        if (!PLAYERS_SLEEPING.containsKey(w))
            return;
        if (!isNight(w))
            return;
        long totalPlayers = w.getPlayers().size();
        if (totalPlayers == 1)
            return;
        final List<Player> sleepingPlayers = PLAYERS_SLEEPING.get(w);
        if (sleepingPlayers.contains(player)) {
            sleepingPlayers.remove(player);
            PLAYERS_SLEEPING.put(w, sleepingPlayers);
            int sleepingCount = sleepingPlayers.size();
            int requiredPlayers = (int) Math.ceil(totalPlayers/2.0);
            w.getPlayers().forEach(pAll -> pAll.sendMessage(this.plugin.getPrefix() + "§e" + player.getName()
                    + " §7schläft nicht mehr §8[§e" + sleepingCount + "§7/" + requiredPlayers + "§8]"));
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> checkSleepingPlayers(w), 5L);
    }

    public void handlePlayerQuit(Player player) {
        final World w = player.getWorld();
        if (!PLAYERS_SLEEPING.containsKey(w))
            return;
        final List<Player> sleepingPlayers = PLAYERS_SLEEPING.get(w);
        if (!sleepingPlayers.contains(player))
            return;
        sleepingPlayers.remove(player);
        PLAYERS_SLEEPING.put(w, sleepingPlayers);
    }

    private void checkSleepingPlayers(World world) {
        long totalPlayers = world.getPlayers().size();
        int sleepingCount = PLAYERS_SLEEPING.getOrDefault(world, new ArrayList<>()).size();
        if (totalPlayers > 0 && sleepingCount >= (totalPlayers/2)) {
            if (!PLAYERS_SLEEPING_RUNNABLES.containsKey(world)) {
                PLAYERS_SLEEPING_RUNNABLES.put(world,
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                            world.setTime(0);
                            world.setStorm(false);
                            world.setThundering(false);
                            sendJoke(world);
                            PLAYERS_SLEEPING.get(world).clear();
                            PLAYERS_SLEEPING_RUNNABLES.get(world).cancel();
                            PLAYERS_SLEEPING_RUNNABLES.remove(world);
                        }, 20L*5));
            }
            world.getPlayers().forEach(pAll -> pAll.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("§aDie Nacht wird übersprungen...")));
        } else {
            if (!PLAYERS_SLEEPING_RUNNABLES.containsKey(world)) {
                return;
            }
            PLAYERS_SLEEPING_RUNNABLES.get(world).cancel();
            PLAYERS_SLEEPING_RUNNABLES.remove(world);
        }
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12000 && time <= 24000;
    }

    private void sendJoke(World world) {
        final Random r = new Random();
        String joke = FLAT_BAD_JOKES.get(r.nextInt(FLAT_BAD_JOKES.size()));
        world.getPlayers().forEach(pAll -> pAll.sendMessage("§e" + joke));
    }

}
