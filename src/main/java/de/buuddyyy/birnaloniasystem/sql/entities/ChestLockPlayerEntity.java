package de.buuddyyy.birnaloniasystem.sql.entities;

import de.buuddyyy.birnaloniasystem.managers.ChestLockManager;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = ChestLockManager.CHESTLOCKS_PLAYERS_TABLE_NAME, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"playerId", "chestLockId"})
})
public class ChestLockPlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @JoinColumn(name = "playerId", referencedColumnName = "id")
    @ManyToOne(targetEntity = PlayerEntity.class, optional = false)
    @Getter
    private PlayerEntity playerEntity;

    @JoinColumn(name = "chestLockId", referencedColumnName = "id")
    @ManyToOne(targetEntity = ChestLockEntity.class, optional = false)
    @Getter
    private ChestLockEntity chestLockEntity;

    public ChestLockPlayerEntity(PlayerEntity playerEntity, ChestLockEntity chestLockEntity) {
        this.playerEntity = playerEntity;
        this.chestLockEntity = chestLockEntity;
    }

    public ChestLockPlayerEntity() {
    }

}
