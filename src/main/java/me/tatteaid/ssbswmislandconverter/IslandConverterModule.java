package me.tatteaid.ssbswmislandconverter;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import me.tatteaid.ssbswmislandconverter.commands.ConvertCommand;
import me.tatteaid.ssbswmislandconverter.converter.ConverterHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class IslandConverterModule extends PluginModule {

    private SuperiorSkyblock plugin;

    private FileConfiguration config;

    private SlimePlugin slimePlugin;
    private SlimeLoader slimeLoader;

    private ConverterHandler converterHandler;

    public IslandConverterModule() {
        super("SWMIslandConverter", "Ambrosia");
    }

    @Override
    public void onEnable(SuperiorSkyblock plugin) {
        this.plugin = plugin;

        saveResource("config.yml");
        this.config = YamlConfiguration.loadConfiguration(new File(getModuleFolder(), "config.yml"));

        this.slimePlugin = (SlimePlugin) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SlimeWorldManager"), "ASWM could not be found on your server...");
        this.slimeLoader = slimePlugin.getLoader(config.getString("data_source"));

        this.converterHandler = new ConverterHandler();
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {

    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        if (getConverterHandler().isTaskRunning()) {
            getLogger().info("Stopping any active conversion tasks...");
            getConverterHandler().setTaskStopped(true);
            getConverterHandler().setTaskRunning(false);
        }
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin) {
        return new SuperiorCommand[]{
                new ConvertCommand(this)
        };
    }

    public void outputInformation(@NotNull String message) {
        Objects.requireNonNull(message, "Message must not be null");
        if (!config.getBoolean("output_information")) return;

        getLogger().info(message);
    }

    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    @NotNull
    public SlimePlugin getSlimePlugin() {
        return slimePlugin;
    }

    public SlimeLoader getSlimeLoader() {
        return slimeLoader;
    }

    public ConverterHandler getConverterHandler() {
        return converterHandler;
    }
}