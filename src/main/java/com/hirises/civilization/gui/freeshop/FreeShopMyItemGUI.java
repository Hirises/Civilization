package com.hirises.civilization.gui.freeshop;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.data.FreeShopItemUnit;
import com.hirises.core.event.GUIUpdateEvent;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIPageContainer;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.store.NBTTagStore;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class FreeShopMyItemGUI extends AbstractGUI {
    private GUIPageContainer container;

    public FreeShopMyItemGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("자유시장_내아이템"));
    }

    @Override
    protected void createInventory() {
        container = new GUIPageContainer("아이템", new Flags<>(GUIContainer.GUIContainerFlags.PREVENT_MODIFY));
        bind("i", container);
        container.setAllPageItem(ConfigManager.shopItem.stream()
                .filter(value -> value.getRegisterUUID().equals(player.getUniqueId()))
                .map(value -> value.getViewItem())
                .collect(Collectors.toList()), container.getInnerSlots().size());
        container.bindOnClick((gui, rawSlot, i1, click, action, guiEventResult) -> {
            ItemStack item = inventory.getItem(rawSlot);
            if(NBTTagStore.containKey(item, Keys.FreeShopItemIndex.toString())){
                int index = NBTTagStore.get(item, Keys.FreeShopItemIndex.toString(), Integer.class);
                FreeShopItemUnit unit = ConfigManager.shopItem.get(index);
                player.getInventory().addItem(unit.getOriginItem());
                ConfigManager.shopItem.remove(index);
                Bukkit.getPluginManager().callEvent(new GUIUpdateEvent(null, FreeShopMyItemGUI.class, false));
                Bukkit.getPluginManager().callEvent(new GUIUpdateEvent(null, FreeShopViewGUI.class, false));
            }
        });

        GUIStateButton previous = new GUIStateButton("이전", "p");
        bind("p", previous);
        previous.bindOnStateChange((gui, i, i1) -> container.previousPage());

        GUIStateButton next = new GUIStateButton("다음", "n");
        bind("n", next);
        next.bindOnStateChange((gui, i, i1) -> container.nextPage());

        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });

        GUIStateButton register = new GUIStateButton("등록", "r");
        bind("r", register);
        register.bindOnStateChange((gui, i, i1) -> {
            new FreeShopRegisterGUI().open(player, gui);
        });
    }

    @Override
    protected void onChange() {
        container.setAllPageItem(ConfigManager.shopItem.stream()
                .filter(value -> value.getRegisterUUID().equals(player.getUniqueId()))
                .map(value -> value.getViewItem())
                .collect(Collectors.toList()), container.getInnerSlots().size());
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
