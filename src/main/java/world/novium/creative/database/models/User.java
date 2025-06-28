package world.novium.creative.database.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User {
    private UUID uniqueId;
    private List<Achievement> achievements;

    public User(UUID uniqueId, List<Achievement> achievements) {
        this.uniqueId = uniqueId;
        this.achievements = achievements;
    }
}
