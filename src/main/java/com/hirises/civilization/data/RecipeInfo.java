package com.hirises.civilization.data;

import com.hirises.civilization.Civilization;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;

public class RecipeInfo implements DataUnit {
    private String[] recipe;
    private Map<String, ItemStack> itemMap = new HashMap<>();
    private ItemStack result;
    private Type recipeType;
    private String key;

    private enum Type{
        Shape,
        Shapeless,
        Furnace,
        Brewing,
        ;
    }

    @Override
    public void load(YamlStore yml, String root) {
        String rawString = yml.get(String.class, root + ".모양");
        recipe = rawString.replace(" ", "").replace(".", " ").split("\n");
        for(String key : yml.getKeys(root + ".심볼")){
            itemMap.put(key, yml.getOrDefault(new ItemStackUnit(), root + ".심볼." + key).getItem());
        }
        result = yml.getOrDefault(new ItemStackUnit(), root + ".결과").getItem();
        recipeType = Type.valueOf(yml.get(String.class, root + ".타입"));
        key = yml.get(String.class, root + ".키");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //ignore
    }

    public void register(){
        switch (recipeType){
            case Shape: {
                ShapedRecipe instance = new ShapedRecipe(new NamespacedKey(Civilization.getInst(), key), result);
                instance.shape(recipe);
                for(String key : itemMap.keySet()){
                    instance.setIngredient(key.charAt(0), itemMap.get(key));
                }
                Bukkit.addRecipe(instance);
                break;
            }
            case Shapeless: {
                ShapelessRecipe instance = new ShapelessRecipe(new NamespacedKey(Civilization.getInst(), key), result);
                for(String key : itemMap.keySet()){
                    instance.addIngredient(key.charAt(0), itemMap.get(key));
                }
                Bukkit.addRecipe(instance);
                break;
            }
        }
    }

    public ItemStack getResult() {
        return result;
    }

    public String[] getRecipe() {
        return recipe;
    }

    public Map<String, ItemStack> getItemMap() {
        return itemMap;
    }
}
