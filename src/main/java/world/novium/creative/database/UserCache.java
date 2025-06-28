package world.novium.creative.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import world.novium.creative.database.models.User;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class UserCache {
    private final Cache<UUID, User> cache;

    private final UserDao userDao;

    public UserCache(UserDao userDao) {
        this.userDao = userDao;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(1000)
                .build();
    }

    public Optional<User> get(UUID uuid) {
        return Optional.ofNullable(cache.getIfPresent(uuid));
    }

    private User getOrLoad(UUID uuid) {
        return cache.get(uuid, key -> {
            Optional<User> user = userDao.getUser(key);

            return user.orElseGet(() -> {
                log.warn("User with UUID {} not found in database, creating new user entry.", key);
                userDao.createUser(key);

                return new User(key, List.of());
            });
        });
    }

    public void put(User user) {
        cache.put(user.getUniqueId(), user);
    }

    public void invalidate(UUID uuid) {
        cache.invalidate(uuid);
    }

    public void save(User user) {
        userDao.saveUser(user);
        put(user);
    }

    public void leave(UUID uuid) {
        Optional<User> user = get(uuid);
        if (user.isPresent()) {
            save(user.get());
            invalidate(uuid);
        }
    }

    public void join(UUID uuid) {
        User user = getOrLoad(uuid);
        put(user);
    }
}
