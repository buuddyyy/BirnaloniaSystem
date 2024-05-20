package de.buuddyyy.birnaloniasystem.handlers;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.managers.ChestLockManager;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockPlayerEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChestLockHandler {

    public static final Map<UUID, EnumChestLockAction> CHEST_LOCK_ACTION_MAP = Maps.newHashMap();
    public static final Map<UUID, PlayerEntity> CHEST_LOCK_TRUST_MAP = Maps.newHashMap();

    private static final List<Material> WHITELIST_MATERIALS = Arrays.asList(
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.BARREL,
            Material.DISPENSER,
            Material.DROPPER,
            Material.HOPPER
    );

    private final BirnaloniaSystemPlugin plugin;
    @Getter private final ChestLockManager chestLockManager;

    public ChestLockHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.chestLockManager = new ChestLockManager(plugin, plugin.getDatabaseManager());
    }

    public void handleInventoryOpen(InventoryOpenEvent event) {
        final Player p = (Player) event.getPlayer();
        if (event.getInventory() == null)
            return;
        final Inventory inv = event.getInventory();
        if (inv.getLocation() == null)
            return;
        final Location loc = inv.getLocation();
        if (!chestLockManager.chestLockExists(loc))
            return;
        final ChestLockEntity chestLockEntity = chestLockManager.getChestLockEntity(loc);
        if (isPlayerTrusted(p, chestLockEntity)) {
            return;
        }
        event.setCancelled(true);
        p.sendMessage(this.plugin.getPrefix() + "§cDu hast keinen Zugriff auf diesen Block!");
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        final Player p = event.getPlayer();
        final Block b = event.getBlock();
        if (!WHITELIST_MATERIALS.contains(b.getType()))
            return;
        final Location loc = b.getLocation();
        if (!chestLockManager.chestLockExists(loc))
            return;
        final ChestLockEntity chestLockEntity = chestLockManager.getChestLockEntity(loc);
        if (!chestLockEntity.getPlayerEntity().getPlayerUuid().equals(p.getUniqueId())) {
            event.setCancelled(true);
            p.sendMessage(this.plugin.getPrefix() + "§cDieser Block gehört dir nicht!");
            return;
        }
        this.chestLockManager.deleteChestLock(chestLockEntity);
        p.sendMessage(this.plugin.getPrefix() + "Du hast die Sicherung entfernt.");
    }

    public void handleBlockPlace(BlockPlaceEvent event) {
        final Player p = event.getPlayer();
        final Block b = event.getBlock();
        if (!WHITELIST_MATERIALS.contains(b.getType()))
            return;
        final Location loc = b.getLocation();
        if (chestLockManager.chestLockExists(loc))
            return;
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.createChestLock(p, b), 1L);
    }

    public void handlePlayerInteract(PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null)
            return;
        final Block b = event.getClickedBlock();
        if (!CHEST_LOCK_ACTION_MAP.containsKey(p.getUniqueId())) {
            return;
        }
        final EnumChestLockAction action = CHEST_LOCK_ACTION_MAP.get(p.getUniqueId());
        CHEST_LOCK_ACTION_MAP.remove(p.getUniqueId());
        event.setCancelled(true);
        final Location loc = b.getLocation();
        if (action == EnumChestLockAction.CREATE) {
            if (chestLockManager.chestLockExists(loc)) {
                p.sendMessage(this.plugin.getPrefix() + "§cDieser Block wurde bereits gesichert!");
                return;
            }
            this.createChestLock(p, b);
            return;
        } else if (action == EnumChestLockAction.REMOVE) {
            if (!chestLockManager.chestLockExists(loc)) {
                p.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert!");
                return;
            }
            final ChestLockEntity chestLockEntity = chestLockManager.getChestLockEntity(loc);
            if (!chestLockEntity.getPlayerEntity().getPlayerUuid().equals(p.getUniqueId())) {
                p.sendMessage(this.plugin.getPrefix() + "§cDir gehört dieser Block nicht!");
                return;
            }
            p.sendMessage(this.plugin.getPrefix() + "§aDu hast die Kiste entsichert.");
            return;
        } else if (action == EnumChestLockAction.TRUST_PLAYER) {
            if (!chestLockManager.chestLockExists(loc)) {
                p.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert!");
                return;
            }
            final ChestLockEntity chestLockEntity = chestLockManager.getChestLockEntity(loc);
            if (!chestLockEntity.getPlayerEntity().getPlayerUuid().equals(p.getUniqueId())) {
                p.sendMessage(this.plugin.getPrefix() + "§cDir gehört dieser Block nicht!");
                return;
            }
            final PlayerEntity otherPlayer = CHEST_LOCK_TRUST_MAP.get(p.getUniqueId());
            CHEST_LOCK_TRUST_MAP.remove(p.getUniqueId());
            if (isPlayerTrusted(otherPlayer.getPlayerUuid(), chestLockEntity)) {
                return;
            }
            ChestLockPlayerEntity trustedChestLockPlayer = new ChestLockPlayerEntity(otherPlayer, chestLockEntity);
            this.chestLockManager.getDatabaseManager().insertEntity(trustedChestLockPlayer);
            this.chestLockManager.updateChestLock(chestLockEntity);
            p.sendMessage(this.plugin.getPrefix() + "");
        } else if (action == EnumChestLockAction.UNTRUST_PLAYER) {

        } else if (action == EnumChestLockAction.INFO) {

        }
    }

    private void createChestLock(Player player, Block block) {
        final PlayerEntity pe = this.plugin.getPlayerHandler().getPlayerManager().getPlayerEntity(player);
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            ChestLockEntity mainLock = new ChestLockEntity(pe, block.getLocation());
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();
                Chest leftChest = (Chest) doubleChest.getLeftSide();
                Chest rightChest = (Chest) doubleChest.getRightSide();
                ChestLockEntity otherLock = null;
                if (block.getLocation().equals(leftChest.getLocation())) {
                    if (!chestLockManager.chestLockExists(rightChest.getLocation()))
                        return;
                    otherLock = chestLockManager.getChestLockEntity(rightChest.getLocation());
                } else if (block.getLocation().equals(rightChest.getLocation())) {
                    if (!chestLockManager.chestLockExists(leftChest.getLocation()))
                        return;
                    otherLock = chestLockManager.getChestLockEntity(leftChest.getLocation());
                }
                if (otherLock == null) {
                    Material prevMat = block.getType();
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(prevMat));
                    player.sendMessage(this.plugin.getPrefix() + "§cEs gab einen Fehler...");
                    return;
                }
                if (!otherLock.getPlayerEntity().getPlayerUuid().equals(player.getUniqueId())) {
                    Material prevMat = block.getType();
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(prevMat));
                    player.sendMessage(this.plugin.getPrefix() + "§cDir gehört diese Kiste nicht!");
                    return;
                }
                mainLock.setTrustedPlayers(otherLock.getTrustedPlayers());
                this.chestLockManager.createChestLock(mainLock);
                player.sendMessage(this.plugin.getPrefix() + "§aDoppelkiste wurde gesichert!");
            }/* else {
                this.chestLockManager.createChestLock(mainLock);
                player.sendMessage(this.plugin.getPrefix() + "§aKiste wurde gesichert!");
            }*/
        }
    }

    private boolean isPlayerTrusted(Player player, ChestLockEntity chestLockEntity) {
        return isPlayerTrusted(player.getUniqueId(), chestLockEntity);
    }

    private boolean isPlayerTrusted(UUID playerUuid, ChestLockEntity chestLockEntity) {
        if (chestLockEntity.getPlayerEntity().getPlayerUuid().equals(playerUuid)) {
            return true;
        }
        if (chestLockEntity.getTrustedPlayers() != null) {
            for (ChestLockPlayerEntity trustedPlayer : chestLockEntity.getTrustedPlayers()) {
                if (trustedPlayer.getPlayerEntity().getPlayerUuid().equals(playerUuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum EnumChestLockAction {
        CREATE,
        REMOVE,
        TRUST_PLAYER,
        UNTRUST_PLAYER,
        INFO
    }

}
