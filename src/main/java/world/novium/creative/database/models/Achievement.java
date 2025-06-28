package world.novium.creative.database.models;

import lombok.Getter;

@Getter
public enum Achievement {
    FIRST_LOGIN("First Login", "Welcome to Novium Creative!"),
    BUILDER("Builder", "Build your first structure"),
    EXPLORER("Explorer", "Explore 10 different plots"),
    COLLECTOR("Collector", "Collect 100 items"),
    COMMUNITY_HELPER("Community Helper", "Help 5 players with their plots");

    private final String title;
    private final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

}
