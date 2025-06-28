package world.novium.creative.common.managers;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class PlotManager {
    private final PlotAPI plotAPI;

    public PlotManager() {
        this.plotAPI = new PlotAPI();
    }

    public Set<Plot> getPlots(Player player) {
        PlotPlayer<?> plotPlayer = PlotPlayer.from(player);
        return plotAPI.getPlayerPlots(plotPlayer);
    }
}
