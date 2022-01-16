package com.hirises.civilization.player;

import com.hirises.civilization.config.Keys;
import com.hirises.civilization.gui.MainGUI;
import com.hirises.core.store.MetaDataStore;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler
    public void onShift_F(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        if(player.isSneaking()){
            event.setCancelled(true);
            new MainGUI().open(player);
        }
    }

    @EventHandler
    public void onLaunchProjectile(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            MetaDataStore.set(event.getEntity(), Keys.ProjectileLauncher.toString(), projectile.getUniqueId());
            ItemStack shooter = player.getInventory().getItemInMainHand();
            if(!ItemUtil.isExist (shooter) || (!shooter.getType().equals(Material.BOW) && !shooter.getType().equals(Material.CROSSBOW)) ){
                shooter = player.getInventory().getItemInOffHand();
            }
            if(!NBTTagStore.containKey(shooter, Keys.EnableItem.toString())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByBlockEvent event){
        Entity taker = event.getEntity();
        Block giver = event.getDamager();
        if(taker instanceof Player){

        }else {

        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        Entity taker = event.getEntity();
        Entity giver = event.getDamager();
        if(taker instanceof Player){
            Util.logging("Adsf");
            if (giver instanceof Player) {
                Player playerGiver = (Player) giver;
                ItemStack weapon = playerGiver.getInventory().getItemInMainHand();
                if (ItemUtil.isExist(weapon) && !NBTTagStore.containKey(weapon, Keys.EnableItem.toString())) {
                    event.setCancelled(true);
                    return;
                }
            } else if (giver instanceof Projectile) {
                Projectile projectileGiver = (Projectile) giver;
                if (!MetaDataStore.containKey(projectileGiver, Keys.ProjectileLauncher.toString())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }else {
            if (giver instanceof Player) {
                Player playerDamager = (Player) giver;
                ItemStack weapon = playerDamager.getInventory().getItemInMainHand();
                if (ItemUtil.isExist(weapon) && !NBTTagStore.containKey(weapon, Keys.EnableItem.toString())) {
                    event.setCancelled(true);
                    return;
                }
            } else if (giver instanceof Projectile) {
                Projectile projectileGiver = (Projectile) giver;
                if (MetaDataStore.containKey(projectileGiver, Keys.ProjectileLauncher.toString())) {
                    if (!MetaDataStore.containKey(projectileGiver, Keys.EnableItem.toString())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
