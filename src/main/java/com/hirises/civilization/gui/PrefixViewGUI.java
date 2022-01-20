package com.hirises.civilization.gui;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.PrefixInfo;
import com.hirises.core.flag.Flags;
import com.hirises.core.inventory.AbstractGUI;
import com.hirises.core.inventory.GUIFlags;
import com.hirises.core.inventory.ui.GUIContainer;
import com.hirises.core.inventory.ui.GUIPageContainer;
import com.hirises.core.inventory.ui.GUIStateButton;

import java.util.stream.Collectors;

public class PrefixViewGUI extends AbstractGUI {
    public PrefixViewGUI() {
        super(new Flags<>(GUIFlags.PREVENT_TOP_INVENTORY_MODIFY), ConfigManager.menu.get("칭호"));
    }

    @Override
    protected void createInventory() {
        GUIPageContainer container = new GUIPageContainer("칭호", new Flags<>(GUIContainer.GUIContainerFlags.PREVENT_MODIFY));
        bind("i", container);
        container.setAllPageItem(ConfigManager.prefixInfo.getSafeDataUnitMap().values().stream()
                .filter(value -> value.isFinish() && value.getFinisher().equals(player.getUniqueId()))
                .map(PrefixInfo::getItem)
                .collect(Collectors.toList()));

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
    }

    @Override
    protected AbstractGUI self() {
        return this;
    }
}
