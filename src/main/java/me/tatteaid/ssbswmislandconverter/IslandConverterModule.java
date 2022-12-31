package me.tatteaid.ssbswmislandconverter;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import me.tatteaid.ssbswmislandconverter.commands.ConvertCommand;
import org.bukkit.event.Listener;

public class IslandConverterModule extends PluginModule {

    protected IslandConverterModule() {
        super("SWMIslandConverter", "Ambrosia");
    }

    @Override
    public void onEnable(SuperiorSkyblock superiorSkyblock) {

    }

    @Override
    public void onReload(SuperiorSkyblock superiorSkyblock) {

    }

    @Override
    public void onDisable(SuperiorSkyblock superiorSkyblock) {

    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock superiorSkyblock) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock superiorSkyblock) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock superiorSkyblock) {
        return new SuperiorCommand[]{
                new ConvertCommand()
        };
    }
}