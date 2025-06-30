package world.novium.creative.common.managers;

import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import world.novium.creative.CreativePlugin;
import world.novium.creative.common.StartupHook;
import world.novium.creative.utils.MessageUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TeamManager implements StartupHook {
    private final ConcurrentHashMap<UUID, Team> playerTeams = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> weightCache = new ConcurrentHashMap<>();

    private final CreativePlugin plugin;

    public TeamManager(CreativePlugin plugin) {
        this.plugin = plugin;
    }

    private LuckPerms luckPerms;

    @Override
    public void onStartup() {
        luckPerms = LuckPermsProvider.get();

        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(plugin, UserDataRecalculateEvent.class, event -> {
            User user = event.getUser();
            UUID uuid = user.getUniqueId();

            if (playerTeams.containsKey(uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    recalculatePlayer(player);
                }
            } else {
                weightCache.put(uuid, getGroupWeight(user));
            }
        });

        eventBus.subscribe(plugin, GroupDataRecalculateEvent.class, event -> {
            Group group = event.getGroup();
            recalculateTeam(group);
        });

        eventBus.subscribe(plugin, GroupCreateEvent.class, event -> {
            Group group = event.getGroup();
            recalculateTeam(group);
        });
    }

    public void recalculateTeam(Group group) {
        if (group == null) return;

        int weight = group.getWeight().orElse(0);
        int invertedWeight = 1000 - weight;
        String weightPrefix = String.format("%03d", invertedWeight);

        playerTeams.forEach((uuid, team) -> {
            if (team.getName().startsWith(weightPrefix)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    recalculatePlayer(player);
                }
            }
        });
    }

    public void recalculatePlayer(Player player) {
        deleteTeam(player);
        addPlayerToTeam(player);
    }

    private void addPlayerToTeam(Player player) {
        Team team = getOrCreateTeam(player);
        team.addEntry(player.getName());
        log.info("Added {} to team {}", player.getName(), team.getName());
    }

    public void deleteTeam(Player player) {
        Team existingTeam = playerTeams.get(player.getUniqueId());
        if (existingTeam != null) {
            existingTeam.unregister();
            playerTeams.remove(player.getUniqueId());
        }

        // Remove from any team via scoreboard API
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team scoreboardTeam = scoreboard.getEntryTeam(player.getName());
        if (scoreboardTeam != null) {
            scoreboardTeam.removeEntry(player.getName());
        }
    }

    private Team getOrCreateTeam(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerTeams.containsKey(uuid)) {
            return updateTeam(playerTeams.get(uuid), player);
        }

        String groupWeight = getPlayerGroupWeight(player);
        String teamName = groupWeight + "-" + player.getName();

        // Max 16 characters for team name
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        Team updated = updateTeam(team, player);
        playerTeams.put(uuid, updated);
        return updated;
    }

    private Team updateTeam(Team team, Player player) {
        String prefix = getPlayerPrefix(player);

        if (!prefix.isEmpty()) {
            team.prefix(MessageUtils.parse(prefix));
        }

        return team;
    }

    private String getPlayerPrefix(Player player) {
        if (luckPerms == null) return "";

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        String primaryGroup = user.getPrimaryGroup();
        Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
        if (group == null) return "";

        String prefix = group.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }



    private String getPlayerGroupWeight(Player player) {
        if (luckPerms == null) return "999";

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        if (group == null) return "999";

        int weight = group.getWeight().orElse(0);
        int invertedWeight = 1000 - weight;
        return String.format("%03d", invertedWeight);
    }

    public int getGroupWeight(User user) {
        UUID uuid = user.getUniqueId();

        if (weightCache.containsKey(uuid)) {
            return weightCache.get(uuid);
        }

        String primaryGroup = user.getPrimaryGroup();
        Group group = luckPerms.getGroupManager().getGroup(primaryGroup);

        int weight = 0;
        if (group != null && group.getWeight().isPresent()) {
            weight = group.getWeight().getAsInt();
        }

        weightCache.put(uuid, weight);
        return weight;
    }
}
