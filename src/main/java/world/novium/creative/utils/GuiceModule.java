package world.novium.creative.utils;


import com.google.inject.AbstractModule;
import lombok.AllArgsConstructor;
import world.novium.creative.CreativePlugin;
import world.novium.creative.database.UserCache;
import world.novium.creative.database.UserDao;
import world.novium.creative.gui.PanelGUI;
import world.novium.creative.gui.SnapshotGUI;
import world.novium.creative.gui.WarpGUI;
import world.novium.creative.managers.PlotManager;
import world.novium.creative.managers.WarpManager;

@AllArgsConstructor
public class GuiceModule extends AbstractModule {

    private final CreativePlugin plugin;

    @Override
    protected void configure() {
        bind(CreativePlugin.class).toInstance(this.plugin);

        bind(PlotManager.class).toInstance(new PlotManager());
        bind(WarpManager.class).toInstance(new WarpManager(this.plugin));
        bind(PanelGUI.class).toInstance(new PanelGUI());
        bind(SnapshotGUI.class).toInstance(new SnapshotGUI());
        bind(WarpGUI.class).toInstance(new WarpGUI());
        UserDao userDao = new UserDao();
        bind(UserDao.class).toInstance(userDao);
        bind(UserCache.class).toInstance(new UserCache(userDao));
    }
}