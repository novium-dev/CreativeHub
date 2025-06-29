package world.novium.creative.utils;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import lombok.AllArgsConstructor;
import world.novium.creative.CreativePlugin;
import world.novium.creative.common.StartupHook;
import world.novium.creative.database.UserCache;
import world.novium.creative.database.UserDao;
import world.novium.creative.gui.PanelGUI;
import world.novium.creative.gui.SnapshotGUI;
import world.novium.creative.gui.WarpGUI;
import world.novium.creative.common.managers.PlotManager;
import world.novium.creative.common.managers.TranslationManager;
import world.novium.creative.common.managers.WarpManager;

@AllArgsConstructor
public class GuiceModule extends AbstractModule {

    private final CreativePlugin plugin;

    @Override
    protected void configure() {
        Multibinder<StartupHook> startupBinder =
                Multibinder.newSetBinder(binder(), StartupHook.class);

        bind(CreativePlugin.class).toInstance(this.plugin);
        bind(PlotManager.class).toInstance(new PlotManager());
        bind(PanelGUI.class).toInstance(new PanelGUI());
        bind(SnapshotGUI.class).toInstance(new SnapshotGUI());
        bind(WarpGUI.class).toInstance(new WarpGUI());

        WarpManager warpManager = new WarpManager(this.plugin);
        bind(WarpManager.class).toInstance(warpManager);
        startupBinder.addBinding().toInstance(warpManager);

        UserDao userDao = new UserDao();
        bind(UserDao.class).toInstance(userDao);
        bind(UserCache.class).toInstance(new UserCache(userDao));

        TranslationManager translationService = new TranslationManager(this.plugin.getDataFolder());
        bind(TranslationManager.class).toInstance(translationService);
        startupBinder.addBinding().toInstance(translationService);
    }
}