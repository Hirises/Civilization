package com.hirises.civilization.gui;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.GUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIStateButton;

public class MainGUI extends GUI {
    public MainGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("메인메뉴"));
    }

    @Override
    protected void createInventory() {
        GUIStateButton ability = new GUIStateButton("숙련도", "a");
        bind("a", ability);
        ability.bindOnStateChange((gui, pre, next) -> {

        });

        GUIStateButton experience = new GUIStateButton("연구", "e");
        bind("e", experience);
        experience.bindOnStateChange((gui, pre, next) -> {

        });

        GUIStateButton wanted = new GUIStateButton("현상금", "w");
        bind("w", wanted);
        wanted.bindOnStateChange((gui, pre, next) -> {
            new PrizeViewGUI().open(player, gui);
        });

        GUIStateButton shop = new GUIStateButton("자유시장", "s");
        bind("s", shop);
        shop.bindOnStateChange((gui, pre, next) -> {

        });
    }
}
