package me.tatteaid.ssbswmislandconverter.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import me.tatteaid.ssbswmislandconverter.IslandConverterModule;
import me.tatteaid.ssbswmislandconverter.converter.ConverterTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ConvertCommand implements SuperiorCommand {

    private final IslandConverterModule instance;

    public ConvertCommand(IslandConverterModule instance) {
        this.instance = instance;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("convert", "converter", "convertislands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.convert.cmd";
    }

    @Override
    public String getUsage(Locale locale) {
        return "admin convert [stop]";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Converts all your islands into individual slime worlds.";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            // start the converter task
            instance.getConverterHandler().setTaskStopped(false);
            instance.getConverterHandler().setTaskRunning(true);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new ConverterTask(instance));
            sender.sendMessage(ChatColor.GRAY + "You have started the conversion process of the SSB2 world to individual ASWM worlds.");
        } else if (args.length == 3 && args[2].equalsIgnoreCase("stop")) {
            if (instance.getConverterHandler().isTaskStopped() || !instance.getConverterHandler().isTaskRunning()) {
                sender.sendMessage(ChatColor.RED + "The conversion task is already cancelled or is not running!");
                return;
            }

            // stop the converter task
            instance.getConverterHandler().setTaskStopped(true);
            instance.getConverterHandler().setTaskRunning(false);
            sender.sendMessage(ChatColor.GRAY + "Stopping conversion process...");
        } else {
            sender.sendMessage(getUsage(Locale.US));
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 3) {
            return Collections.singletonList("stop");
        }

        return Collections.emptyList();
    }
}