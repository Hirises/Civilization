package com.hirises.civilization.gui;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.GUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIPageContainer;
import com.hirises.core.inventory.ui.GUIStateButton;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrizeViewGUI extends GUI {
    public PrizeViewGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("현상금"));
    }

    @Override
    protected void createInventory() {
        GUIPageContainer container = new GUIPageContainer("현상금", new Flags<>(GUIContainer.GUIContainerFlags.PREVENT_MODIFY));
        bind("i", container);
        List<ItemStack> items = new ArrayList<>();
        List<OfflinePlayer> players =
                Arrays.stream(Bukkit.getOfflinePlayers())
                        .distinct()
                        .sorted(Comparator.comparingLong(value -> -1 * ConfigManager.getCache(value.getUniqueId()).getKillRewardModifier()))
                        .collect(Collectors.toList());
        for(OfflinePlayer player : players){
            items.add(getHeadItem(player));
        }
        container.setAllPageItem(items, container.getInnerSlots().size());

        GUIStateButton previous = new GUIStateButton("이전", "p");
        bind("p", previous);
        previous.bindOnStateChange((gui, i, i1) -> {
           container.previousPage();
        });

        GUIStateButton next = new GUIStateButton("다음", "n");
        bind("n", next);
        next.bindOnStateChange((gui, i, i1) -> {
            container.nextPage();
        });

        GUIStateButton back = new GUIStateButton("뒤로", "-");
        bind("-", back);
        back.bindOnStateChange((gui, i, i1) -> {
            gui.close();
        });
    }

    @Override
    protected GUI self() {
        return this;
    }

    private ItemStack getHeadItem(OfflinePlayer player){
        ItemStack item = getItemBySymbol("h").clone();
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        ItemUtil.remapString(item, Util.toRemap(
                "name", player.getName(),
                "prize", String.valueOf(ConfigManager.getCache(player.getUniqueId()).getKillRewardModifier()))
        );
        return item;
    }
}
