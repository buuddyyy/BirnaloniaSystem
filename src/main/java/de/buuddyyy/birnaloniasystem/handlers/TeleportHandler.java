package de.buuddyyy.birnaloniasystem.handlers;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.config.MainConfig;
import de.buuddyyy.birnaloniasystem.managers.HomeManager;
import de.buuddyyy.birnaloniasystem.sql.entities.HomeEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TeleportHandler {

    private static final Map<UUID, BukkitTask> PLAYER_TELEPORT_RUNNABLES = Maps.newHashMap();
    private static final Map<UUID, List<UUID>> PLAYER_TELEPORT_REQUESTS = Maps.newHashMap();

    private final BirnaloniaSystemPlugin plugin;
    private final PlayerHandler playerHandler;
    @Getter private final HomeManager homeManager;

    private int teleportDelay = 3; // 3 seconds to teleport
    private Location spawnLocation;

    public TeleportHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerHandler = plugin.getPlayerHandler();
        this.homeManager = new HomeManager(plugin, plugin.getDatabaseManager());
        this.spawnLocation = null;
        this.loadFromConfig();
    }

    public void sendTeleportRequest(Player player, Player toPlayer) {
        final List<UUID> requestLists = PLAYER_TELEPORT_REQUESTS.getOrDefault(toPlayer.getUniqueId(), new ArrayList<>());
        if (requestLists.contains(player.getUniqueId())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDu hast bereits eine Teleport-Anfrage verschickt!");
            return;
        }
        requestLists.add(player.getUniqueId());
        PLAYER_TELEPORT_REQUESTS.put(toPlayer.getUniqueId(), requestLists);
        TextComponent requestCommand = new TextComponent("§8[§aAnnehmen§8]");
        requestCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/tpaccept " + player.getName()));
        requestCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent("§7Teleport-Anfrage von §e" + player.getName() + " §7annehmen")
        }));
        toPlayer.spigot().sendMessage(new TextComponent(this.plugin.getPrefix() + "§e" + player.getName()
                + " §7hat dir eine §eTeleport-Anfrage §7geschickt: "), requestCommand);
        player.sendMessage(this.plugin.getPrefix() + "Du hast eine Teleport-Anfrage an §e"
                + toPlayer.getName() + " §7verschickt.");
    }

    public void teleportToPlayer(Player player, Player fromPlayer) {
        if (PLAYER_TELEPORT_RUNNABLES.containsKey(player.getUniqueId())) {
            return;
        }
        final List<UUID> requestLists = PLAYER_TELEPORT_REQUESTS.get(player.getUniqueId());
        if (!requestLists.contains(fromPlayer.getUniqueId())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDu hast keine Teleport-Anfrage von §e"
                    + fromPlayer.getName() + " §cerhalten!");
            return;
        }
        requestLists.remove(fromPlayer.getUniqueId());
        PLAYER_TELEPORT_REQUESTS.put(player.getUniqueId(), requestLists);
        player.sendMessage(this.plugin.getPrefix() + "§aDu hast die Teleport-Anfrage von §e"
                + fromPlayer.getName() + " §aangenommen.");
        fromPlayer.sendMessage(this.plugin.getPrefix() + "§e" + player.getName() + " §ahat deine "
                + "Teleport-Anfrage angenommen.");
        this.teleportPlayer(fromPlayer, player.getLocation());
    }

    public void teleportPlayerToHome(Player player, String homeName) {
        final Map<String, HomeEntity> map = this.homeManager.getHomes(player.getUniqueId());
        if (!map.containsKey(homeName)) {
            return;
        }
        final HomeEntity homeEntity = map.get(homeName);
        this.teleportPlayer(player, homeEntity.getLocation());
    }

    public void teleportPlayerToSpawn(Player player) {
        if (this.spawnLocation == null) {
            player.sendMessage(this.plugin.getPrefix() + "§eSpawn-Location §cwurde noch nicht gesetzt!");
            return;
        }
        this.teleportPlayer(player, this.spawnLocation);
    }

    public void setPlayerHome(Player player, String homeName, Location location) {
        final Map<String, HomeEntity> map = this.homeManager.getHomes(player.getUniqueId());
        final PlayerEntity pe = this.playerHandler.getPlayerManager().getPlayerEntity(player);
        if (map.containsKey(homeName)) {
            HomeEntity homeEntity = map.get(homeName);
            homeEntity.setLocation(location);
            this.plugin.getDatabaseManager().updateEntity(homeEntity);
        } else {
            HomeEntity homeEntity = new HomeEntity(pe, homeName, location);
            this.plugin.getDatabaseManager().insertEntity(homeEntity);
        }
        this.homeManager.getPlayerHomes().refresh(player.getUniqueId());
    }

    public boolean existsPlayerHome(Player player, String homeName) {
        return this.homeManager.getHomes(player.getUniqueId()).containsKey(homeName);
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
        final MainConfig config = this.plugin.getMainConfig();
        final FileConfiguration fc = config.getConfiguration();
        fc.set("spawn.worldName", location.getWorld().getName());
        fc.set("spawn.x", location.getX());
        fc.set("spawn.y", location.getY());
        fc.set("spawn.z", location.getZ());
        fc.set("spawn.yaw", location.getYaw());
        fc.set("spawn.pitch", location.getPitch());
        config.save();
    }

    public void handleMove(PlayerMoveEvent event) {
        final Player p = event.getPlayer();
        if (!PLAYER_TELEPORT_RUNNABLES.containsKey(p.getUniqueId())) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        final Location fromLoc = event.getFrom();
        final Location toLoc = event.getTo();
        if (fromLoc.getX() == toLoc.getX()
                && fromLoc.getBlockY() == toLoc.getBlockY()
                && fromLoc.getZ() == toLoc.getZ()) {
            return;
        }
        PLAYER_TELEPORT_RUNNABLES.get(p.getUniqueId()).cancel();
        PLAYER_TELEPORT_RUNNABLES.remove(p.getUniqueId());
        p.sendMessage(this.plugin.getPrefix() + "§cDu hast dich bewegt. " +
                "Teleportation wurde abgebrochen!");
    }

    public void teleportToSpawn(Player player) {
        if (this.spawnLocation == null)
            return;
        player.teleport(this.spawnLocation);
    }

    private void teleportPlayer(Player player, Location location) {
        if (PLAYER_TELEPORT_RUNNABLES.containsKey(player.getUniqueId())) {
            return;
        }
        PLAYER_TELEPORT_RUNNABLES.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(plugin,
                () -> {
                    player.teleport(location);
                    PLAYER_TELEPORT_RUNNABLES.get(player.getUniqueId()).cancel();
                    PLAYER_TELEPORT_RUNNABLES.remove(player.getUniqueId());
                }, 20L*teleportDelay));
        player.sendMessage(this.plugin.getPrefix() + "Du wirst §7teleportiert. " +
                "Du darfst dich §e" + teleportDelay + " Sekunden §7lang nicht bewegen.");
    }

    private void loadFromConfig() {
        final MainConfig config = this.plugin.getMainConfig();
        final FileConfiguration fc = config.getConfiguration();
        if (fc.contains("spawn")) {
            String worldName = fc.getString("spawn.worldName");
            World w = Bukkit.getWorld(worldName);
            double x = fc.getDouble("spawn.x");
            double y = fc.getDouble("spawn.y");
            double z = fc.getDouble("spawn.z");
            float yaw = (float) fc.getDouble("spawn.yaw");
            float pitch = (float) fc.getDouble("spawn.pitch");
            final Location loc = new Location(w, x, y, z);
            loc.setYaw(yaw);
            loc.setPitch(pitch);
            this.spawnLocation = loc;
        }
        if (fc.contains("teleport-delay")) {
            this.teleportDelay = fc.getInt("teleport-delay");
        }
    }

}
