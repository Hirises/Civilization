package com.hirises.civilization.gui.ability;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.AbilityInfo;
import com.hirises.civilization.data.AbilityType;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityCategoryGUI extends AbstractGUI {
    public AbilityCategoryGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("숙련도메뉴"));
    }

    @Override
    protected void createInventory() {
        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        GUIStateButton move = new GUIStateButton("이동", "1");
        bind("1", move);
        move.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Move, player);
            addNewItemSymbol("1", getAbilityItem(AbilityType.Move, player));
        });
        addNewItemSymbol("1", getAbilityItem(AbilityType.Move, player));

        GUIStateButton attack = new GUIStateButton("전투", "2");
        bind("2", attack);
        attack.bindOnStateChange((gui, i, i1) -> {
            new CombatAbilityGUI().open(player, gui);
        });

        GUIStateButton mining = new GUIStateButton("채광", "3");
        bind("3", mining);
        mining.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Mine, player);
            addNewItemSymbol("3", getAbilityItem(AbilityType.Mine, player));
        });
        addNewItemSymbol("3", getAbilityItem(AbilityType.Mine, player));

        GUIStateButton craft = new GUIStateButton("공예", "4");
        bind("4", craft);
        craft.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Craft, player);
            addNewItemSymbol("4", getAbilityItem(AbilityType.Craft, player));
        });
        addNewItemSymbol("4", getAbilityItem(AbilityType.Craft, player));

        GUIStateButton farming = new GUIStateButton("농사", "5");
        bind("5", farming);
        farming.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Farm, player);
            addNewItemSymbol("5", getAbilityItem(AbilityType.Farm, player));
        });
        addNewItemSymbol("5", getAbilityItem(AbilityType.Farm, player));

        GUIStateButton cultivating = new GUIStateButton("목축", "6");
        bind("6", cultivating);
        cultivating.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Cultivate, player);
            addNewItemSymbol("6", getAbilityItem(AbilityType.Cultivate, player));
        });
        addNewItemSymbol("6", getAbilityItem(AbilityType.Cultivate, player));

        GUIStateButton fishing = new GUIStateButton("낚시", "7");
        bind("7", fishing);
        fishing.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Fishing, player);
            addNewItemSymbol("7", getAbilityItem(AbilityType.Fishing, player));
        });
        addNewItemSymbol("7", getAbilityItem(AbilityType.Fishing, player));

        GUIStateButton potion = new GUIStateButton("양조", "8");
        bind("8", potion);
        potion.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Potion, player);
            addNewItemSymbol("8", getAbilityItem(AbilityType.Potion, player));
        });
        addNewItemSymbol("8", getAbilityItem(AbilityType.Potion, player));

        GUIStateButton intelligent = new GUIStateButton("지능", "9");
        bind("9", intelligent);
        intelligent.bindOnStateChange((gui, i, i1) -> {
            checkLevelUp(AbilityType.Intelligent, player);
            addNewItemSymbol("9", getAbilityItem(AbilityType.Intelligent, player));
        });
        addNewItemSymbol("9", getAbilityItem(AbilityType.Intelligent, player));
    }

    public static ItemStack getAbilityItem(AbilityType type, Player player){
        ItemStack item = ConfigManager.abilityItem.clone();
        AbilityInfo info = ConfigManager.abilityInfo.get(type.getName());
        item.setType(info.getItem());
        ItemUtil.remapString(item, Util.toRemap("type", type.getName(), "howToGet", info.getHowToGet(), "curEffects", info.getEffectString(),
            "level", String.valueOf( ConfigManager.getCache(player.getUniqueId()).getAbilityLevel(type) ), "cost", String.valueOf(info.getCost()) ));
        return item;
    }

    public static void checkLevelUp(AbilityType type, Player player){
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        AbilityInfo info = ConfigManager.abilityInfo.get(type.getName());
        if(player.getLevel() >= info.getCost()){
            player.setLevel(player.getLevel() - info.getCost());

            cache.addAbilityLevel(type);
        }
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
