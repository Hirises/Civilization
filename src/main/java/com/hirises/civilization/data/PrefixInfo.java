package com.hirises.civilization.data;

import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PrefixInfo implements DataUnit {
    ItemStack item;
    String name;
    int id;
    UUID finisher;

    @Override
    public void load(YamlStore yml, String root) {
        this.name = yml.get(String.class, root + ".이름");
        if(yml.containKey(root + ".완료")){
            this.finisher = UUID.fromString(yml.get(String.class, root + ".완료"));
        }else{
            this.finisher = null;
        }
        this.id = Integer.parseInt(root.substring(root.lastIndexOf(".") + 1));
        this.item = yml.getOrDefault(new ItemStackUnit(), root + ".아이템").getItem();
    }

    @Override
    public void save(YamlStore yml, String root) {
        if(isFinish()){
            yml.set(root + ".완료", finisher.toString());
        }else{
            yml.removeKey(root + ".완료");
        }
    }

    public void setFinisher(UUID finisher) {
        this.finisher = finisher;
    }

    public UUID getFinisher() {
        return finisher;
    }

    public boolean isFinish(){
        return finisher != null;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
