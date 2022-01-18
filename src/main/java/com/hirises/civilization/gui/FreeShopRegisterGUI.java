package com.hirises.civilization.gui;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.util.ItemUtil;
import org.bukkit.inventory.ItemStack;

public class FreeShopRegisterGUI extends AbstractGUI {
    public FreeShopRegisterGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("자유시장_등록"));
    }

    @Override
    protected void createInventory() {
        GUIContainer container = new GUIContainer("아이템", new Flags<>(GUIContainer.GUIContainerFlags.RETURN_ITEM_WHEN_CLOSE));
        bind("i", container);

        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        GUIStateButton register = new GUIStateButton("등록", "r");
        bind("r", register);
        register.bindOnStateChange((gui, i, i1) -> {
            ItemStack item = container.getInnerItems().get(0);
            if(ItemUtil.isExist(item)){
                new FreeShopCostGUI(item).open(player, gui);
            }
        });
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
