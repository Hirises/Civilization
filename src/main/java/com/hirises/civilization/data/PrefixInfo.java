package com.hirises.civilization.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.Material;

public class PrefixInfo implements DataUnit {
    String name;
    Material material;
    String effect;
    String trigger;
    PrefixType type;

    @Override
    public void load(YamlStore yml, String root) {
        this.type = PrefixType.valueOf(yml.get(String.class, root + ".타입"));
        this.name = yml.get(String.class, root + ".이름");
        this.material = Material.valueOf(yml.get(String.class, root + ".코드"));
        this.effect = yml.get(String.class, root + ".효과");
        this.trigger = yml.get(String.class, root + ".조건");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //ignore
    }

    public PrefixType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public String getEffect() {
        return effect;
    }

    public String getTrigger() {
        return trigger;
    }
}
