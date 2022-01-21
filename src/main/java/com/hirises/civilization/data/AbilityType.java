package com.hirises.civilization.data;

public enum AbilityType {
    Move("이동"),
    Fight("전투"),
    Mine("건축"),
    Craft("공예"),
    Farm("농사"),
    Cultivate("목축"),
    Fishing("낚시"),
    Potion("양조"),
    Intelligent("지능"),
    ;

    private String name;

    AbilityType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
