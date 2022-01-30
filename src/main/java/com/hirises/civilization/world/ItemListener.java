package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.data.Structure;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.Util;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.stream.Collectors;

public class ItemListener implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent event){
        if(!Civilization.isStart()){
            return;
        }
        ItemStack item = event.getCurrentItem();
        if(NBTTagStore.containKey(item, Keys.CustomItem.toString())){
            String custom = NBTTagStore.get(item, Keys.CustomItem.toString(), String.class);
            switch (custom){
                case "Crack_Compass":{
                    CompassMeta meta = (CompassMeta) item.getItemMeta();
                    Structure structure = ConfigManager.structureList.values().stream().filter(value -> value.getType().startsWith("crack")).limit(1).collect(Collectors.toList()).get(0);
                    Location loc = new Location(structure.getStructureInfo().getWorld().get(), structure.getMinX(), structure.getMinY(), structure.getMinZ());
                    meta.setLodestone(loc);
                    meta.setLodestoneTracked(false);
                    item.setItemMeta(meta);
                    break;
                }
            }
        }
    }
}
