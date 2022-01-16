package com.hirises.civilization.config;

public enum Keys{
    MoneyItem("Civilization_MoneyItem")
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
