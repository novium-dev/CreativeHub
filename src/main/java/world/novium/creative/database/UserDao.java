package world.novium.creative.database;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import world.novium.creative.database.models.Achievement;
import world.novium.creative.database.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class UserDao {
    public Optional<User> getUser(@NotNull UUID uuid) {
        List<Achievement> achievements = Query.query("SELECT name FROM achievements WHERE user_unique_id = ?")
                .single(Call.of().bind(uuid.toString()))
                .map(row -> Achievement.valueOf(row.getString("name")))
                .all();

        return Query.query("SELECT * FROM users WHERE unique_id = ?")
                .single(Call.of().bind(uuid.toString()))
                .map(row -> new User(
                        UUID.fromString(row.getString("unique_id")),
                        achievements.isEmpty() ? List.of() : achievements
                ))
                .first();
    }

    public void addAchievement(@NotNull UUID uuid, @NotNull Achievement achievement) {
        Query.query("INSERT INTO achievements (user_unique_id, name) VALUES (?, ?)")
                .single(Call.of()
                        .bind(uuid.toString())
                        .bind(achievement.name()))
                .insert();
    }

    public void createUser(@NotNull UUID uuid) {
        Query.query("INSERT INTO users (unique_id) VALUES (?)")
                .single(Call.of().bind(uuid.toString()))
                .insert();

        log.info("Created user with UUID: {}", uuid);
    }

    public void saveUser(User user) {
        Query.query("UPDATE users SET unique_id = ? WHERE unique_id = ?")
                .single(Call.of()
                        .bind(user.getUniqueId().toString())
                        .bind(user.getUniqueId().toString()))
                .update();
    }
}
