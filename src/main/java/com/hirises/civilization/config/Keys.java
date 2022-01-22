package com.hirises.civilization.config;

public enum Keys{
    MoneyItem("Civilization_MoneyItem"),
    FreeShopItemIndex("Civilization_FreeShopItemIndex"),
    StaminaHeal("Civilization_StaminaHeal"),
    CustomItem("Civilization_CustomItem"),
    ;

    private String key;

    Keys(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
