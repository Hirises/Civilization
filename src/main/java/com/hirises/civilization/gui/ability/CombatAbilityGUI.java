package com.hirises.civilization.gui.ability;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.AbilityType;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIStateButton;

public class CombatAbilityGUI extends AbstractGUI {
    public CombatAbilityGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("전투숙련도"));
    }

    @Override
    protected void createInventory() {
        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        GUIStateButton mining = new GUIStateButton("칼", "3");
        bind("3", mining);
        mining.bindOnStateChange((gui, i, i1) -> {
            AbilityCategoryGUI.checkLevelUp(AbilityType.Sword, player);
            addNewItemSymbol("3", AbilityCategoryGUI.getAbilityItem(AbilityType.Sword, player));
        });
        addNewItemSymbol("3", AbilityCategoryGUI.getAbilityItem(AbilityType.Sword, player));


        GUIStateButton farming = new GUIStateButton("도끼", "5");
        bind("5", farming);
        farming.bindOnStateChange((gui, i, i1) -> {
            AbilityCategoryGUI.checkLevelUp(AbilityType.Axe, player);
            addNewItemSymbol("5", AbilityCategoryGUI.getAbilityItem(AbilityType.Axe, player));
        });
        addNewItemSymbol("5", AbilityCategoryGUI.getAbilityItem(AbilityType.Axe, player));


        GUIStateButton fishing = new GUIStateButton("원거리", "7");
        bind("7", fishing);
        fishing.bindOnStateChange((gui, i, i1) -> {
            AbilityCategoryGUI.checkLevelUp(AbilityType.Bow, player);
            addNewItemSymbol("7", AbilityCategoryGUI.getAbilityItem(AbilityType.Bow, player));
        });
        addNewItemSymbol("7", AbilityCategoryGUI.getAbilityItem(AbilityType.Bow, player));
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
