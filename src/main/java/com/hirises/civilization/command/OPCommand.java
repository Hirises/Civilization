package com.hirises.civilization.command;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.world.NMSSupport;
import com.hirises.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPCommand implements CommandExecutor, TabCompleter {
    private static final Map<CommandSender, CommandType> commandMap = new HashMap<>();

    private enum CommandType{
        Start,
        Reset,
        None;
    }

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
                commandMap.put(sender, CommandType.Start);
                Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                    commandMap.remove(sender);
                }, 200);
                sender.sendMessage(ChatColor.RED + "정말 게임을 시작하시겠습니까? 진행사항이 초기화됩니다." + ChatColor.GRAY + " (/civilization confirm)");
                break;
            }
            case "reset": {
                commandMap.put(sender, CommandType.Reset);
                Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                    commandMap.remove(sender);
                }, 200);
                sender.sendMessage(ChatColor.RED + "정말 게임을 초기화하시겠습니까?" + ChatColor.GRAY + " (/civilization confirm)");
                break;
            }
            case "confirm": {
                if(commandMap.containsKey(sender)){
                    switch (commandMap.get(sender)){
                        case Reset: {
                            Civilization.resetGame();
                            break;
                        }
                        case Start: {
                            Civilization.startGame();
                            break;
                        }
                    }
                }
                break;
            }
            case "reload": {
                Util.broadcast(new TextComponent(ChatColor.RED + "리로드를 시작합니다"));
                ConfigManager.init();
                Util.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));
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
                allOutputs = new String[]{"start", "reset", "confirm", "reload"};
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