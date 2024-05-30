package de.buuddyyy.birnaloniasystem.events;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {

    private final BirnaloniaSystemPlugin plugin;

    public BlockExplodeListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        this.plugin.getChestLockHandler().handleExplode(event.blockList(), event);
    }

}
