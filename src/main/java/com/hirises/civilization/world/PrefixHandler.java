package com.hirises.civilization.world;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.PrefixInfo;
import org.bukkit.event.Listener;

import java.util.UUID;

public class PrefixHandler implements Listener {

    public static boolean hasPrefix(UUID uuid, int id){
        PrefixInfo info = ConfigManager.prefixInfo.get(String.valueOf(id));
        return info.isFinish() && info.getFinisher().equals(uuid);
    }


}
