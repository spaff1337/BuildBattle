package me.spaff.buildbattle.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class Utils {
    public static void clearPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            clearPlayer(player);
        });
    }

    public static void clearPlayer(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);
        player.setExhaustion(0.0f);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFlySpeed(0.1f);
        player.setFireTicks(0);

        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getOpenInventory().setCursor(null);

        player.closeInventory();
    }
}