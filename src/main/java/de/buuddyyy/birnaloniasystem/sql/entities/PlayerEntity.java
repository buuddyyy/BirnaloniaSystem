package de.buuddyyy.birnaloniasystem.sql.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "players")
public final class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private UUID playerUuid;
    private Timestamp lastOnline;
    private boolean chestLockActive;

    public PlayerEntity(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.setChestLockActive(false);
    }

    public int getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Timestamp getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Timestamp lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isChestLockActive() {
        return chestLockActive;
    }

    public void setChestLockActive(boolean chestLockActive) {
        this.chestLockActive = chestLockActive;
    }

}
