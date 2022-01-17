package com.hirises.civilization.command;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class OPCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if(!sender.isOp()){
            return false;
        }
        switch (args[0]) {
            case "start": {
                Civilization.startGame();
                break;
            }
            case "reset": {
                Civilization.resetGame();
                break;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String[] allOutputs;
        ArrayList<String> output = new ArrayList<>();
        switch (args.length) {
            case 1: {
                allOutputs = new String[]{"start", "reset"};
                for (String str : allOutputs) {
                    if (str.startsWith(args[0])) {
                        output.add(str);
                    }
                }
                break;
            }
        }
        return output;
    }
}