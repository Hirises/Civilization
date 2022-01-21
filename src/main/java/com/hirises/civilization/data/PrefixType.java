package com.hirises.civilization.data;

public enum PrefixType {
    First_Death("최초의사망"),
    First_Kill("최초의살인"),
    ;

    private String key;

    PrefixType(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
