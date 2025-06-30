package world.novium.creative.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.google.inject.Inject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import world.novium.creative.common.managers.SettingsManager;
import world.novium.creative.common.managers.TeamManager;
import world.novium.creative.database.UserCache;
import world.novium.creative.utils.MessageUtils;
import world.novium.creative.common.annotations.RegisterListener;

@RegisterListener
public class PlayerListeners implements Listener {
    @Inject
    private UserCache userCache;

    @Inject
    private SettingsManager settingsManager;

    @Inject
    private TeamManager teamManager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        userCache.join(event.getPlayer().getUniqueId());
        event.joinMessage(MessageUtils.parse("<gray>[<green>+</green>] <white>" + event.getPlayer().getName()));

        teamManager.recalculatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(MessageUtils.parse("<gray>[<red>-</red>] <white>" + event.getPlayer().getName()));
        userCache.leave(event.getPlayer().getUniqueId());

        teamManager.deleteTeam(event.getPlayer());
    }

    @EventHandler
    public void onList(PaperServerListPingEvent event) {
        String fixedLine = "           <gradient:#ff0000:#ff9900>Creative Hub</gradient><color:#30303d> • <color:#ff3845>Work In Progress\n";

        event.motd(MessageUtils.parse(
                fixedLine +
                        "             <#428bff>Join our <#434755>Discord <#428bff>for updates"
        ));

        if (settingsManager.getSettings().isMaintenanceMode()) {
            event.setMaxPlayers(0);
            event.setProtocolVersion(-1);
            event.setVersion("§cMaintenance Mode");
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        LuckPerms lp = LuckPermsProvider.get();

        User user = lp.getUserManager().getUser(event.getUniqueId());

        if (user == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageUtils.parse("<red>Unable to retrieve user data!"));
            return;
        }

        CachedPermissionData permissionData = user.getCachedData().getPermissionData();

        if (settingsManager.getSettings().isMaintenanceMode() && !permissionData.checkPermission("novium.maintenance.bypass").asBoolean()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageUtils.parse("<red>Server is in maintenance mode!"));
        }
    }
}
