package world.novium.creative.utils;

import lombok.extern.slf4j.Slf4j;
import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.novium.creative.CreativePlugin;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import world.novium.creative.annotations.RegisterListener;
import world.novium.creative.commands.Command;
import world.novium.creative.annotations.RegisterCommand;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Slf4j
public class ServiceRegistry {
    private final CreativePlugin plugin;
    private final ClassLoader classLoader;
    private final Injector injector;

    public ServiceRegistry(CreativePlugin plugin, ClassLoader classLoader, Injector injector) {
        this.plugin = plugin;
        this.classLoader = classLoader;
        this.injector = injector;
    }

    public void registerAllListeners() {
        AtomicInteger successCases = new AtomicInteger();
        Set<Class<?>> listenerClasses = StreamSupport.stream(ClassIndex.getAnnotated(RegisterListener.class, this.classLoader).spliterator(), false)
                .filter(Listener.class::isAssignableFrom)
                .collect(Collectors.toSet());
        if (listenerClasses.isEmpty()) {
            plugin.getLogger().warning("No listeners found to register.");
            return;
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        listenerClasses.forEach(listenerClass -> {
            pluginManager.registerEvents((Listener) this.injector.getInstance(listenerClass), this.plugin);
            successCases.getAndIncrement();
        });

        log.info("Registered listeners: {}/{}", successCases.get(), listenerClasses.size());
    }

    public void registerAllCommands() {
        AtomicInteger successCases = new AtomicInteger();
        Set<Class<?>> commandClasses = StreamSupport.stream(ClassIndex.getAnnotated(RegisterCommand.class, this.classLoader).spliterator(), false)
                .filter(Command.class::isAssignableFrom)
                .collect(Collectors.toSet());;
        if (commandClasses.isEmpty()) {
            plugin.getLogger().warning("No commands found to register.");
            return;
        }

        commandClasses.forEach(commandClass -> {
            Command commandInstance = (Command) this.injector.getInstance(commandClass);
            commandInstance.build().register();
            successCases.getAndIncrement();
        });

        log.info("Registered commands: {}/{}", successCases.get(), commandClasses.size());
    }
}
