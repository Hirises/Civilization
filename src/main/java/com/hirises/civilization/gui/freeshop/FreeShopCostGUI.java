package com.hirises.civilization.gui.freeshop;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.FreeShopItemUnit;
import com.hirises.core.event.GUIUpdateEvent;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIEventResult;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.util.ItemUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class FreeShopCostGUI extends AbstractGUI {
    private ItemStack target;

    public FreeShopCostGUI(ItemStack target) {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("자유시장_가격설정"));
        this.target = target;
    }

    @Override
    protected void createInventory() {
        AnvilInventory anvil = (AnvilInventory) inventory;
        anvil.setRepairCost(0);

        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        GUIContainer input = new GUIContainer("수정방지", new Flags<>(GUIContainer.GUIContainerFlags.PREVENT_MODIFY));
        bind("i", input);
        input.bindOnClick((gui, i, i1, clickType, inventoryAction, guiEventResult) -> {
           close();
        });

        GUIContainer output = new GUIContainer("출력", new Flags<>(GUIContainer.GUIContainerFlags.PREVENT_MODIFY));
        bind("o", output);
        output.bindOnClick((gui, i, i1, clickType, inventoryAction, guiEventResult) -> {
            AnvilInventory an = (AnvilInventory) gui.getInventory();
            String rawPriceString = an.getRenameText();
            if(NumberUtils.isNumber(rawPriceString)){
                long price = Long.parseLong(rawPriceString);
                ((GUIContainer)parent.getUI("아이템")).setInnerItems(new ArrayList<>());
                FreeShopItemUnit unit = new FreeShopItemUnit(target, price, player);
                ConfigManager.shopItem.add(unit);
                Bukkit.getPluginManager().callEvent(new GUIUpdateEvent(null, FreeShopMyItemGUI.class, false));
                Bukkit.getPluginManager().callEvent(new GUIUpdateEvent(null, FreeShopViewGUI.class, false));
                close();
            }else{
                guiEventResult.setCanceled(true);
                an.setFirstItem(ItemUtil.setDisplayName(an.getFirstItem(), ChatColor.WHITE + "Invalid Value"));
            }
        });
    }

    @Override
    protected void onClose(GUIEventResult result) {
        AnvilInventory anvil = (AnvilInventory) inventory;
        anvil.setFirstItem(new ItemStack(Material.AIR));
        anvil.setSecondItem(new ItemStack(Material.AIR));
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
