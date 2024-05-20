package de.buuddyyy.birnaloniasystem.sql.entities;

import de.buuddyyy.birnaloniasystem.managers.ChestLockManager;
import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = ChestLockManager.CHESTLOCKS_TABLE_NAME, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"worldName", "blockX", "blockY", "blockZ"})
})
public final class ChestLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JoinColumn(name = "ownerPlayerId", referencedColumnName = "id")
    @ManyToOne(targetEntity = PlayerEntity.class, optional = false)
    private PlayerEntity playerEntity;
    private String worldName;
    private int blockX;
    private int blockY;
    private int blockZ;

    @Transient
    @OneToMany(mappedBy = "playerId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChestLockPlayerEntity> trustedPlayers;

    public ChestLockEntity(PlayerEntity playerEntity, Location location) {
        this.playerEntity = playerEntity;
        this.worldName = location.getWorld().getName();
        this.blockX = location.getBlockX();
        this.blockY = location.getBlockY();
        this.blockZ = location.getBlockZ();
        this.trustedPlayers = new ArrayList<>();
    }

    public ChestLockEntity() {
        this.trustedPlayers = new ArrayList<>();
    }

    public PlayerEntity getPlayerEntity() {
        return playerEntity;
    }

    public Location getLocation() {
        final World world = Bukkit.getWorld(worldName);
        return new Location(world, blockX, blockY, blockZ);
    }

    public List<ChestLockPlayerEntity> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void setTrustedPlayers(List<ChestLockPlayerEntity> trustedPlayers) {
        this.trustedPlayers.addAll(trustedPlayers);
    }

}
