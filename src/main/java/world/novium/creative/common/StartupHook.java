package world.novium.creative.common;

public interface StartupHook {
    /**
     * Called after the plugin has finished enabling.
     */
    void onStartup();
}