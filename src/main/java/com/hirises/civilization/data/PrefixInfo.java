package com.hirises.civilization.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.Material;

public class PrefixInfo implements DataUnit {
    private String name;
    private Material material;
    private String effect;
    private String trigger;
    private PrefixType type;
    private int exp;

    @Override
    public void load(YamlStore yml, String root) {
        this.type = PrefixType.valueOf(yml.get(String.class, root + ".타입"));
        this.name = yml.get(String.class, root + ".이름");
        this.material = Material.valueOf(yml.get(String.class, root + ".코드"));
        this.effect = yml.get(String.class, root + ".효과");
        this.trigger = yml.get(String.class, root + ".조건");
        this.exp = yml.get(Integer.class, root + ".경험치");
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

    public int getExp() {
        return exp;
    }
}
