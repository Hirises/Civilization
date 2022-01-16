package com.hirises.civilization.gui;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class FreeShopItemUnit implements DataUnit {
    ItemStack originItem;
    UUID registerUUID;
    String registerName;
    long price;

    public FreeShopItemUnit(){

    }

    public FreeShopItemUnit(ItemStack item, long price, Player register){
        originItem = item;
        this.price = price;
        this.registerName = register.getName();
        this.registerUUID = register.getUniqueId();
    }

    @Override
    public void load(YamlStore yml, String s) {
        originItem = yml.getOrDefault(new ItemStackUnit(), s).getItem();
        price = yml.get(Long.class, s + ".가격");
        registerUUID = UUID.fromString(yml.get(String.class, s + ".UUID"));
        registerName = yml.get(String.class, s + ".등록자");

    }

    @Override
    public void save(YamlStore yml, String s) {
        yml.upsert(new ItemStackUnit(originItem), s);
        yml.set(s + ".가격", price);
        yml.set(s + ".UUID", registerUUID);
        yml.set(s + ".등록자", registerName);
    }

    public ItemStack getOriginItem() {
        return originItem;
    }

    public ItemStack getShopItem() {
        return ItemUtil.appendLore(
                NBTTagStore.set(originItem.clone(), Keys.FreeShopItemIndex.toString(), ConfigManager.shopItem.indexOf(this))
                , Arrays.asList("", ChatColor.YELLOW + "" + price + ChatColor.GRAY + "원", ChatColor.GRAY + registerName));
    }

    public ItemStack getViewItem() {
        return ItemUtil.appendLore(
                NBTTagStore.set(originItem.clone(), Keys.FreeShopItemIndex.toString(), ConfigManager.shopItem.indexOf(this))
                , Arrays.asList("", ChatColor.YELLOW + "" + price + ChatColor.GRAY + "원", ChatColor.DARK_RED + "클릭시 등록 해제"));
    }

    public long getPrice() {
        return price;
    }

    public String getRegisterName() {
        return registerName;
    }

    public UUID getRegisterUUID() {
        return registerUUID;
    }
}
