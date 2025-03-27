package me.spaff.buildbattle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;

public class GameListener implements Listener {
    // Blocks
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent e) {
        e.setCancelled(true);
    }

    // Entities
    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (!e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        e.getEntity().remove();
    }
}
