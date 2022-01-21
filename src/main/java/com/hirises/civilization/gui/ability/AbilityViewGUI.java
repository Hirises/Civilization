package com.hirises.civilization.gui.ability;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.AbilityType;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.inventory.ItemStack;

public class AbilityViewGUI extends AbstractGUI {
    private AbilityType type;

    public AbilityViewGUI(AbilityType type) {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("숙련도보기"));
    }

    @Override
    protected void createInventory() {
        setTitle(Util.remapString(getTitle(), "title", type.getName()));

        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        setItemSymbol(getItemSlotBySymbol("0").get(0), String.valueOf(type.ordinal() + 1));

        ItemStack levelInfo = inventory.getItem(getItemSlotBySymbol("l").get(0));
        ItemUtil.remapString(levelInfo, Util.toRemap("title", type.getName()));


    }

    @Override
    protected AbstractGUI self() {
        return null;
    }
}
