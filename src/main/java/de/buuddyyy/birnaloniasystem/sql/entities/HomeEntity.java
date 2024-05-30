package de.buuddyyy.birnaloniasystem.sql.entities;

import de.buuddyyy.birnaloniasystem.managers.HomeManager;
import jakarta.persistence.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Entity
@Table(name = HomeManager.TABLE_NAME, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id", "name"})
})
public final class HomeEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Getter
    @JoinColumn(name = "playerId", referencedColumnName = "id")
    @ManyToOne(targetEntity = PlayerEntity.class, optional = false)
    private PlayerEntity playerEntity;
    @Getter
    private String name;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public HomeEntity(PlayerEntity playerEntity, String name, Location location) {
        this.playerEntity = playerEntity;
        this.name = name;
        this.setLocation(location);
    }

    public HomeEntity() {
    }

    public void setLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public Location getLocation() {
        final World world = Bukkit.getWorld(this.worldName);
        final Location location = new Location(world, this.x, this.y, this.z);
        location.setYaw(this.yaw);
        location.setPitch(this.pitch);
        return location;
    }

}
