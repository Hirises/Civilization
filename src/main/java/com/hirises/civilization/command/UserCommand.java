package com.hirises.civilization.command;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.gui.MainGUI;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.store.NBTTagStore;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UserCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;
        switch (label){
            case "menu":
                new MainGUI().open(player);
                return true;
            case "money":
                if(args.length < 1){
                    player.sendMessage("/money <amount>");
                    return false;
                }
                PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
                if(!NumberUtils.isNumber(args[0])){
                    player.sendMessage("amount must be number " + ChatColor.GRAY + "input: " + args[0]);
                    return false;
                }
                long amount = Long.parseLong(args[0]);
                if(cache.hasMoney(amount)){
                    cache.operateMoney(-amount);
                    player.getInventory().addItem(NBTTagStore.set(ConfigManager.getMoneyItem(amount), Keys.MoneyItem.toString(), amount));
                    return true;
                }else{
                    player.sendMessage("you don't have enough money " + ChatColor.GRAY + "need: " + args[0]);
                    return false;
                }
        }
        return false;
    }
}