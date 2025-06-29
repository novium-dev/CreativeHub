package world.novium.creative.common;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Settings {
    private boolean maintenanceMode = false;

    public Settings(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}