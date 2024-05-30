package de.buuddyyy.birnaloniasystem.sql.entities;

import de.buuddyyy.birnaloniasystem.managers.PlayerManager;
import de.buuddyyy.birnaloniasystem.sql.converters.UUIDConverter;
import jakarta.persistence.*;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = PlayerManager.TABLE_NAME)
public final class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, columnDefinition = "CHAR(36)")
    @Convert(converter = UUIDConverter.class)
    private UUID playerUuid;

    @Setter
    private String playerName;

    @Setter
    private Timestamp lastOnline;

    @Setter
    private boolean chestLockActive;

    public PlayerEntity(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.setChestLockActive(false);
    }

    public PlayerEntity() {
    }

    public int getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Timestamp getLastOnline() {
        return lastOnline;
    }

    public boolean isChestLockActive() {
        return chestLockActive;
    }

}
