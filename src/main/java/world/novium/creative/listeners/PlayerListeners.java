package world.novium.creative.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import world.novium.creative.common.managers.SettingsManager;
import world.novium.creative.database.UserCache;
import world.novium.creative.utils.MessageUtils;
import world.novium.creative.common.annotations.RegisterListener;

@RegisterListener
public class PlayerListeners implements Listener {
    @Inject
    private UserCache userCache;

    @Inject
    private SettingsManager settingsManager;

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

    @EventHandler
    public void onList(PaperServerListPingEvent event) {
        event.motd(MessageUtils.parse(
                "           <gradient:#ff0000:#ff9900>Creative Hub</gradient><color:#30303d> • <color:#ff3845>Work In Progress\n" +
                        "             <#428bff>Join our <#434755>Discord <#428bff>for updates"
        ));

        if (settingsManager.getSettings().isMaintenanceMode()) {
            event.setMaxPlayers(0);
            event.setProtocolVersion(-1); // Set to -1 to indicate maintenance mode
            event.setVersion("§cMaintenance Mode");
        }
    }
}
