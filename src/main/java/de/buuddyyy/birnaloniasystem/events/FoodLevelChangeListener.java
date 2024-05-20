package de.buuddyyy.birnaloniasystem.events;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Map;

public class FoodLevelChangeListener implements Listener {

    private static final int LOSE_HUNGER = 5;
    private Map<Player, Integer> playerHungerMap;
    private final BirnaloniaSystemPlugin plugin;

    public FoodLevelChangeListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerHungerMap = Maps.newHashMap();
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        final Player p = (Player) event.getEntity();
        if (event.getItem() == null) {
            int counter = 0;
            if (this.playerHungerMap.containsKey(p)) {
                counter = this.playerHungerMap.get(p);
            }
            if (counter <= LOSE_HUNGER) {
                event.setCancelled(true);
                counter++;
                this.playerHungerMap.put(p, counter);
                return;
            }
        }
        this.playerHungerMap.put(p, 0);
     }

}
