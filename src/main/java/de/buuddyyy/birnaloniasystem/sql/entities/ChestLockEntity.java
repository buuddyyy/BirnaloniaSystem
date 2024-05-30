package de.buuddyyy.birnaloniasystem.sql.entities;

import de.buuddyyy.birnaloniasystem.managers.ChestLockManager;
import jakarta.persistence.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Entity
@Table(name = ChestLockManager.CHESTLOCKS_TABLE_NAME, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"worldName", "blockX", "blockY", "blockZ"})
})
public final class ChestLockEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @JoinColumn(name = "ownerPlayerId", referencedColumnName = "id")
    @ManyToOne(targetEntity = PlayerEntity.class, optional = false)
    private PlayerEntity ownerPlayerEntity;
    private String worldName;
    private int blockX;
    private int blockY;
    private int blockZ;

    public ChestLockEntity(PlayerEntity playerEntity, Location location) {
        this.ownerPlayerEntity = playerEntity;
        this.worldName = location.getWorld().getName();
        this.blockX = location.getBlockX();
        this.blockY = location.getBlockY();
        this.blockZ = location.getBlockZ();
    }

    public ChestLockEntity() {
    }

    public Location getLocation() {
        final World world = Bukkit.getWorld(worldName);
        return new Location(world, blockX, blockY, blockZ);
    }

}
