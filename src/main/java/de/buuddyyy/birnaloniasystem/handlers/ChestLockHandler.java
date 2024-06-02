package de.buuddyyy.birnaloniasystem.handlers;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.managers.ChestLockManager;
import de.buuddyyy.birnaloniasystem.managers.PlayerManager;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockPlayerEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestLockHandler {

    public static final Map<UUID, EnumChestLockAction> CHEST_LOCK_ACTION_MAP = Maps.newHashMap();
    public static final Map<UUID, PlayerEntity> CHEST_LOCK_TRUST_MAP = Maps.newHashMap();

    private static final List<Material> WHITELIST_MATERIALS = Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.BARREL,
            Material.DROPPER,
            Material.DISPENSER,
            Material.HOPPER
    );

    private final BirnaloniaSystemPlugin plugin;
    private final ChestLockManager chestLockManager;
    private final PlayerManager playerManager;

    public ChestLockHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.chestLockManager = new ChestLockManager(plugin, plugin.getDatabaseManager());
        this.playerManager = this.plugin.getPlayerHandler().getPlayerManager();
    }

    public void handleExplode(List<Block> block, Cancellable cancellable) {
        final List<Block> toDelete = new ArrayList<>();
        for (Block b : block) {
            if (!WHITELIST_MATERIALS.contains(b.getType()))
                continue;
            if (!this.chestLockManager.chestLockExists(b.getLocation()))
                continue;
            toDelete.add(b);
        }
        block.removeAll(toDelete);
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        final Player p = event.getPlayer();
        final Block b = event.getBlock();
        if (!WHITELIST_MATERIALS.contains(b.getType()))
            return;
        final Location loc = b.getLocation();
        if (!this.chestLockManager.chestLockExists(loc))
            return;
        final ChestLockEntity cle = this.chestLockManager.getChestLockEntity(loc);
        if (p.isOp() && p.getGameMode() == GameMode.CREATIVE) {
            this.chestLockManager.deleteChestLock(cle);
            p.sendMessage(this.plugin.getPrefix() + "§cBlock wurde entsichert.");
            return;
        }
        if (!cle.getOwnerPlayerEntity().getPlayerUuid().equals(p.getUniqueId())
                || !(p.isOp() && p.getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
            p.sendMessage(this.plugin.getPrefix() + "§cDir gehört dieser Block nicht.");
            return;
        }
        this.chestLockManager.deleteChestLock(cle);
        p.sendMessage(this.plugin.getPrefix() + "§cBlock wurde entsichert.");
    }

    public void handleBlockPlace(BlockPlaceEvent event) {
        final Player p = event.getPlayer();
        final Block b = event.getBlock();
        if (!WHITELIST_MATERIALS.contains(b.getType()))
            return;
        final Location loc = b.getLocation();
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
            if (this.plugin.getPlayerHandler().isAutoChestLockEnabled(p)) {
                this.createChestLock(p, b);
            }
            return;
        }
        final Chest chest = (Chest) b.getState();
        final PlayerEntity pe = this.playerManager.getPlayerEntity(p);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (!isDoubleChest(b)) {
                if (this.plugin.getPlayerHandler().isAutoChestLockEnabled(p)) {
                    this.createChestLock(p, b);
                }
                return;
            }
            if (!isDoubleChestLocked(b)) {
                return;
            }
            final DoubleChest doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();
            final Chest leftChest = (Chest) doubleChest.getLeftSide();
            final Chest rightChest = (Chest) doubleChest.getRightSide();
            ChestLockEntity newChestLockEntity = new ChestLockEntity(pe, loc);
            ChestLockEntity oldChestLockEntity = null;
            if (loc.equals(leftChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(rightChest.getLocation());
            } else if (loc.equals(rightChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(leftChest.getLocation());
            }
            if (oldChestLockEntity == null) {
                Material prevType = b.getType();
                b.setType(Material.AIR);
                b.getWorld().dropItemNaturally(loc, new ItemStack(prevType));
                p.sendMessage(this.plugin.getPrefix() + "§cEs gab einen Fehler...");
                return;
            }
            if (!oldChestLockEntity.getOwnerPlayerEntity().getPlayerUuid().equals(p.getUniqueId())) {
                Material prevType = b.getType();
                b.setType(Material.AIR);
                b.getWorld().dropItemNaturally(loc, new ItemStack(prevType));
                p.sendMessage(this.plugin.getPrefix() + "§cDie gehört diese Kiste nicht!");
                return;
            }
            this.chestLockManager.createChestLock(newChestLockEntity);
            final List<ChestLockPlayerEntity> playerEntities = this.chestLockManager.getTrustedPlayer(oldChestLockEntity);
            playerEntities.forEach(trustedPlayers -> this.chestLockManager.createTrustedPlayer(
                    new ChestLockPlayerEntity(trustedPlayers.getPlayerEntity(), newChestLockEntity)));
            p.sendMessage(this.plugin.getPrefix() + "§eDoppelkiste §7wurde gesichert.");
        }, 1L);
    }

    public void handlePlayerInteract(PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null)
            return;
        final Block b = event.getClickedBlock();
        if (CHEST_LOCK_ACTION_MAP.containsKey(p.getUniqueId())) {
            final EnumChestLockAction action = CHEST_LOCK_ACTION_MAP.get(p.getUniqueId());
            CHEST_LOCK_ACTION_MAP.remove(p.getUniqueId());
            event.setCancelled(true);
            if (action == EnumChestLockAction.CREATE) {
                this.createChestLock(p, b);
            } else if (action == EnumChestLockAction.REMOVE) {
                this.removeChestLock(p, b);
            } else if (action == EnumChestLockAction.TRUST_PLAYER) {
                this.trustPlayer(p, b);
            } else if (action == EnumChestLockAction.UNTRUST_PLAYER) {
                this.untrustPlayer(p, b);
            } else if (action == EnumChestLockAction.INFO) {
                this.showInfo(p, b);
            }
            return;
        }
        final Location loc = b.getLocation();
        if (!WHITELIST_MATERIALS.contains(loc.getBlock().getType())
                || !chestLockManager.chestLockExists(loc))
            return;
        final ChestLockEntity cle = this.chestLockManager.getChestLockEntity(loc);
        if (isPlayerTrusted(p, cle)) {
            return;
        }
        event.setCancelled(true);
        p.sendMessage(this.plugin.getPrefix() + "§cDu hast keinen Zugriff auf diesen Block.");
    }

    private void createChestLock(Player player, Block block) {
        if (this.chestLockManager.chestLockExists(block.getLocation())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist bereits gesichert.");
            return;
        }
        final Location loc = block.getLocation();
        final PlayerEntity pe = this.playerManager.getPlayerEntity(player);
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            ChestLockEntity cle = new ChestLockEntity(pe, loc);
            this.chestLockManager.createChestLock(cle);
            player.sendMessage(this.plugin.getPrefix() + "§eBlock §7wurde gesichert.");
            return;
        }
        final Chest chest = (Chest) block.getState();
        if (isDoubleChest(block)) {
            final DoubleChest doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();
            final Chest leftChest = (Chest) doubleChest.getLeftSide();
            final Chest rightChest = (Chest) doubleChest.getRightSide();
            ChestLockEntity newChestLockEntity = new ChestLockEntity(pe, loc);
            ChestLockEntity oldChestLockEntity = null;
            if (loc.equals(leftChest.getLocation())) {
                if (this.chestLockManager.chestLockExists(rightChest.getLocation())) {
                    oldChestLockEntity = this.chestLockManager.getChestLockEntity(rightChest.getLocation());
                } else {
                    oldChestLockEntity = new ChestLockEntity(pe, rightChest.getLocation());
                    this.chestLockManager.createChestLock(oldChestLockEntity);
                }
            } else if (loc.equals(rightChest.getLocation())) {
                if (this.chestLockManager.chestLockExists(leftChest.getLocation())) {
                    oldChestLockEntity = this.chestLockManager.getChestLockEntity(leftChest.getLocation());
                } else {
                    oldChestLockEntity = new ChestLockEntity(pe, leftChest.getLocation());
                    this.chestLockManager.createChestLock(oldChestLockEntity);
                }
            }
            this.chestLockManager.createChestLock(newChestLockEntity);
            final List<ChestLockPlayerEntity> playerEntities = this.chestLockManager.getTrustedPlayer(oldChestLockEntity);
            playerEntities.forEach(trustedPlayers -> this.chestLockManager.createTrustedPlayer(
                    new ChestLockPlayerEntity(trustedPlayers.getPlayerEntity(), newChestLockEntity)));
            player.sendMessage(this.plugin.getPrefix() + "§eDoppelkiste §7wurde gesichert.");
        } else {
            ChestLockEntity cle = new ChestLockEntity(pe, loc);
            this.chestLockManager.createChestLock(cle);
            player.sendMessage(this.plugin.getPrefix() + "§eKiste §7wurde gesichert.");
        }

    }

    private void removeChestLock(Player player, Block block) {
        if (!this.chestLockManager.chestLockExists(block.getLocation())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert.");
            return;
        }
        final Location loc = block.getLocation();
        final ChestLockEntity cle = this.chestLockManager.getChestLockEntity(loc);
        if (!cle.getOwnerPlayerEntity().getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDir gehört diese Kiste nicht.");
            return;
        }
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            player.sendMessage(this.plugin.getPrefix() + "§cBlock wurde entsichert.");
        } else {
            final Chest chest = (Chest) block.getState();
            if (isDoubleChest(block)) {
                final DoubleChest doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();
                final Chest leftChest = (Chest) doubleChest.getLeftSide();
                final Chest rightChest = (Chest) doubleChest.getRightSide();
                ChestLockEntity oldChestLockEntity = null;
                if (loc.equals(leftChest.getLocation())) {
                    if (this.chestLockManager.chestLockExists(rightChest.getLocation())) {
                        oldChestLockEntity = this.chestLockManager.getChestLockEntity(rightChest.getLocation());
                    }
                } else if (loc.equals(rightChest.getLocation())) {
                    if (this.chestLockManager.chestLockExists(leftChest.getLocation())) {
                        oldChestLockEntity = this.chestLockManager.getChestLockEntity(leftChest.getLocation());
                    }
                }
                this.chestLockManager.deleteChestLock(oldChestLockEntity);
                player.sendMessage(this.plugin.getPrefix() + "§eDoppelkiste §7wurde entsichert.");
            } else {
                player.sendMessage(this.plugin.getPrefix() + "§eKiste §7wurde entsichert.");
            }
        }
        this.chestLockManager.deleteChestLock(cle);
    }

    private void trustPlayer(Player player, Block block) {
        final PlayerEntity targetPlayer = this.getToTrustedPlayer(player);
        if (targetPlayer == null) {
            player.sendMessage(this.plugin.getPrefix() + "§cEs gab einen Fehler...");
            return;
        }
        final Location loc = block.getLocation();
        if (!this.chestLockManager.chestLockExists(loc)) {
            player.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert.");
            return;
        }
        final ChestLockEntity cle = this.chestLockManager.getChestLockEntity(loc);
        if (!cle.getOwnerPlayerEntity().getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDir gehört diese Kiste nicht.");
            return;
        }
        if (isDoubleChest(block)) {
            final DoubleChest doubleChest = ((DoubleChestInventory) ((Chest) block.getState())
                    .getInventory()).getHolder();
            final Chest leftChest = (Chest) doubleChest.getLeftSide();
            final Chest rightChest = (Chest) doubleChest.getRightSide();
            ChestLockEntity oldChestLockEntity = null;
            if (loc.equals(leftChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(rightChest.getLocation());
            } else if (loc.equals(rightChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(leftChest.getLocation());
            }
            if (isPlayerTrusted(targetPlayer.getPlayerUuid(), oldChestLockEntity)) {
                player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName()
                        + " §cist bereits getrusted.");
                return;
            }
            this.chestLockManager.createTrustedPlayer(new ChestLockPlayerEntity(targetPlayer, oldChestLockEntity));
        } else {
            if (isPlayerTrusted(targetPlayer.getPlayerUuid(), cle)) {
                player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName()
                        + " §cist bereits getrusted.");
                return;
            }
        }
        final ChestLockPlayerEntity clpe = new ChestLockPlayerEntity(targetPlayer, cle);
        this.chestLockManager.createTrustedPlayer(clpe);
        player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName() + " §aist nun getrusted.");
    }

    private void untrustPlayer(Player player, Block block) {
        final PlayerEntity targetPlayer = this.getToTrustedPlayer(player);
        if (targetPlayer == null) {
            player.sendMessage(this.plugin.getPrefix() + "§cEs gab einen Fehler...");
            return;
        }
        final Location loc = block.getLocation();
        if (!this.chestLockManager.chestLockExists(loc)) {
            player.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert.");
            return;
        }
        final ChestLockEntity cle = this.chestLockManager.getChestLockEntity(loc);
        if (!cle.getOwnerPlayerEntity().getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDir gehört diese Kiste nicht.");
            return;
        }
        if (isDoubleChest(block)) {
            final DoubleChest doubleChest = ((DoubleChestInventory) ((Chest) block.getState())
                    .getInventory()).getHolder();
            final Chest leftChest = (Chest) doubleChest.getLeftSide();
            final Chest rightChest = (Chest) doubleChest.getRightSide();
            ChestLockEntity oldChestLockEntity = null;
            if (loc.equals(leftChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(rightChest.getLocation());
            } else if (loc.equals(rightChest.getLocation())) {
                oldChestLockEntity = this.chestLockManager.getChestLockEntity(leftChest.getLocation());
            }
            if (!isPlayerTrusted(targetPlayer.getPlayerUuid(), oldChestLockEntity)) {
                player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName()
                        + " §cist nicht auf dieser Kiste getrusted.");
                return;
            }
            this.chestLockManager.deleteTrustedPlayer(this.chestLockManager
                    .getChestLockPlayer(targetPlayer, oldChestLockEntity));
        } else {
            if (!isPlayerTrusted(targetPlayer.getPlayerUuid(), cle)) {
                player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName()
                        + " §cist nicht auf dieser Kiste getrusted.");
                return;
            }
        }
        final ChestLockPlayerEntity clpe = this.chestLockManager.getChestLockPlayer(targetPlayer, cle);
        this.chestLockManager.deleteTrustedPlayer(clpe);
        player.sendMessage(this.plugin.getPrefix() + "§e" + targetPlayer.getPlayerName()
                + " §chat nun nicht mehr getrusted.");
    }

    private void showInfo(Player player, Block block) {
        if (!this.chestLockManager.chestLockExists(block.getLocation())) {
            player.sendMessage(this.plugin.getPrefix() + "§cDieser Block ist nicht gesichert.");
            return;
        }
        final ChestLockEntity chestLock = this.chestLockManager.getChestLockEntity(block.getLocation());
        final List<ChestLockPlayerEntity> trustedPlayers = this.chestLockManager.getTrustedPlayer(chestLock);
        StringBuilder sb = new StringBuilder();
        if (!trustedPlayers.isEmpty()) {
            for (ChestLockPlayerEntity trustedPlayer : trustedPlayers) {
                sb.append("§e").append(trustedPlayer.getPlayerEntity().getPlayerName())
                        .append("§7").append(", ");
            }
            sb.setLength(sb.toString().length()-2);
        }
        String worldName = chestLock.getLocation().getWorld().getName();
        int x = chestLock.getLocation().getBlockX();
        int y = chestLock.getLocation().getBlockY();
        int z = chestLock.getLocation().getBlockZ();
        player.sendMessage(
                this.plugin.getPrefix() + "§eChestLock Info:",
                "§7Besitzer: §e" + chestLock.getOwnerPlayerEntity().getPlayerName(),
                "§7Koordinaten: §eX: §6" + x + " §eY: §6" + y + " §eZ: §6" + z + " §eWelt: §6" + worldName,
                "§7Trusted: " + (trustedPlayers.isEmpty() ? "§e-" : sb.toString())
        );
    }

    private boolean isPlayerTrusted(Player player, ChestLockEntity chestLockEntity) {
        return isPlayerTrusted(player.getUniqueId(), chestLockEntity) ||
                (player.isOp() && player.getGameMode() == GameMode.CREATIVE);
    }

    private boolean isPlayerTrusted(UUID playerUuid, ChestLockEntity chestLockEntity) {
        if (chestLockEntity.getOwnerPlayerEntity().getPlayerUuid().equals(playerUuid))
            return true;
        final List<ChestLockPlayerEntity> trustedPlayers = this.chestLockManager.getTrustedPlayer(chestLockEntity);
        for (ChestLockPlayerEntity trustedPlayer : trustedPlayers) {
            if (playerUuid.equals(trustedPlayer.getPlayerEntity().getPlayerUuid()))
                return true;
        }
        return false;
    }

    private boolean isDoubleChest(Block block) {
        if (block.getType() != Material.CHEST
                && block.getType() != Material.TRAPPED_CHEST) {
            return false;
        }
        final Chest chest = (Chest) block.getState();
        return chest.getInventory().getHolder() instanceof DoubleChest;
    }

    private boolean isDoubleChestLocked(Block block) {
        if (!isDoubleChest(block))
            return false;
        final DoubleChest doubleChest = ((DoubleChestInventory) ((Chest) block.getState())
                .getInventory()).getHolder();
        if (doubleChest == null)
            return false;
        final Chest leftChest = (Chest) doubleChest.getLeftSide();
        final Chest rightChest = (Chest) doubleChest.getRightSide();
        if (leftChest == null || rightChest == null)
            return false;
        if (block.getLocation().equals(leftChest.getLocation())) {
            return this.chestLockManager.chestLockExists(rightChest.getLocation());
        } else if (block.getLocation().equals(rightChest.getLocation())) {
            return this.chestLockManager.chestLockExists(leftChest.getLocation());
        }
        return false;
    }

    private PlayerEntity getToTrustedPlayer(Player player) {
        if (!CHEST_LOCK_TRUST_MAP.containsKey(player.getUniqueId())) {
            return null;
        }
        PlayerEntity pe = CHEST_LOCK_TRUST_MAP.get(player.getUniqueId());
        CHEST_LOCK_TRUST_MAP.remove(player.getUniqueId());
        return pe;
    }

    public enum EnumChestLockAction {
        CREATE,
        REMOVE,
        TRUST_PLAYER,
        UNTRUST_PLAYER,
        INFO
    }

}
