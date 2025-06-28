package world.novium.creative.listeners;

import com.google.inject.Inject;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import world.novium.creative.managers.WorldManager;
import world.novium.creative.annotations.RegisterListener;

@RegisterListener
public class WorldListeners implements Listener {

    @Inject
    private WorldManager worldManager;

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        World world = event.getFrom();

        int playerCount = world.getPlayers().size();

        if (playerCount == 0 && worldManager.getLoadedWorld(world.getName()) != null) {
            worldManager.unloadWorld(event.getPlayer(), true);
        }
    }
}
