package world.novium.creative.common;

import lombok.Getter;

@Getter
public class Translation {
    private final String key;
    private final String value;

    public Translation(String key, String value) {
        this.key = key;
        this.value = value;
    }


}
