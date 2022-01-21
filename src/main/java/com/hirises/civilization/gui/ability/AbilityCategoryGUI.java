package com.hirises.civilization.gui.ability;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIStateButton;

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

        });

        GUIStateButton attack = new GUIStateButton("전투", "2");
        bind("2", attack);
        attack.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton mining = new GUIStateButton("채광", "3");
        bind("3", mining);
        mining.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton craft = new GUIStateButton("공예", "4");
        bind("4", craft);
        craft.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton farming = new GUIStateButton("농사", "5");
        bind("5", farming);
        farming.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton cultivating = new GUIStateButton("목축", "6");
        bind("6", cultivating);
        cultivating.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton fishing = new GUIStateButton("낚시", "7");
        bind("7", fishing);
        fishing.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton potion = new GUIStateButton("양조", "8");
        bind("8", potion);
        potion.bindOnStateChange((gui, i, i1) -> {

        });

        GUIStateButton intelligent = new GUIStateButton("지능", "9");
        bind("9", intelligent);
        intelligent.bindOnStateChange((gui, i, i1) -> {

        });
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
