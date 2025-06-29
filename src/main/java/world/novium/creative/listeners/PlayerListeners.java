package world.novium.creative.listeners;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import world.novium.creative.database.UserCache;
import world.novium.creative.utils.MessageUtils;
import world.novium.creative.common.annotations.RegisterListener;

@RegisterListener
public class PlayerListeners implements Listener {
    @Inject
    private UserCache userCache;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        userCache.join(event.getPlayer().getUniqueId());
        event.joinMessage(MessageUtils.parse("<gray>[<green>+</green>] <white>" + event.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(MessageUtils.parse("<gray>[<red>-</red>] <white>" + event.getPlayer().getName()));
        userCache.leave(event.getPlayer().getUniqueId());
    }

}
