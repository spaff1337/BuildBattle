package me.spaff.buildbattle.listener;

import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.arena.Arena;
import me.spaff.buildbattle.manager.ScoreboardManager;
import me.spaff.buildbattle.server.game.Plot;
import me.spaff.buildbattle.server.game.Game;
import me.spaff.buildbattle.server.game.Setup;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.BukkitUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        e.setJoinMessage(null);

        if (Main.getServerManager().isGameServer()) {
            if (!Game.getInstance().canJoin() && !player.isOp()) {
                player.kickPlayer("You are not allowed on the server!");
                return;
            }

            if (player.isOp() && !FileManager.isFileInDirectory(FileManager.getPluginDirectory(), "arena")) {
                BukkitUtils.sendMessage(player, "&eGame was not configured for this server yet!");
                return;
            }

            Arena.getInstance().join(player);
        }
        else if (Main.getServerManager().isHubServer()) {
            if (player.isOp() && !FileManager.isFileInDirectory(FileManager.getPluginDirectory(), "lobby")) {
                BukkitUtils.sendMessage(player, "&eLobby configuration is not set up for this server yet!");
                return;
            }

            Main.getServerManager().getLobby().getSpawnLocation().ifPresent(location -> player.teleport(location));
            ScoreboardManager.updateScoreboard(player);

            Main.getServerManager().getLobby().getPlayNPC().ifPresent(npc -> npc.show(player));
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if (Main.getServerManager().isGameServer())
            Arena.getInstance().leave(e.getPlayer());

        BBPlayer.getInstance(e.getPlayer()).clearInstance();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        e.setCancelled(true);

        if (!(e.getEntity() instanceof Player player)) return;
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) return;

        if (Main.getServerManager().isGameServer()) {
            if (Game.getInstance().isWaiting() || Game.getInstance().isStarting()) {
                player.teleport(Main.getServerManager().getGameData().getWaitingLocation());
            }
            else {
                if (Game.getInstance().isVotingPhase())
                    player.teleport(Game.getInstance().getCurrentlyJudgedPlot().getCenter());
                else
                    player.teleport(Game.getInstance().getPlayerPlot(BBPlayer.getInstance(player)).getCenter());
            }
        }
        else if (Main.getServerManager().isHubServer()) {
            Main.getServerManager().getLobby().getSpawnLocation().ifPresent(location -> player.teleport(location));
        }
    }

    // Block Actions
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        BBPlayer bbPlayer = BBPlayer.getInstance(e.getPlayer());
        Game game = Game.getInstance();

        if (!game.isInProgress()) {
            e.setCancelled(!bbPlayer.hasRestrictionsBypass());
            return;
        }

        if (game.isVotingPhase()) {
            e.setCancelled(true);
            return;
        }

        Plot plot = game.getPlayerPlot(bbPlayer);
        if (plot == null || !BukkitUtils.isBetweenLocations(block.getLocation(), plot.getBoundaryA(), plot.getBoundaryB())) {
            e.setCancelled(true);
            BukkitUtils.sendMessage(player, "&cYou cannot break this block!");
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        Game game = Game.getInstance();
        BBPlayer bbPlayer = BBPlayer.getInstance(e.getPlayer());

        if (!game.isInProgress()) {
            e.setCancelled(!bbPlayer.hasRestrictionsBypass());
            return;
        }

        if (game.isVotingPhase()) {
            e.setCancelled(true);
            return;
        }

        Plot plot = game.getPlayerPlot(bbPlayer);
        if (plot == null || !BukkitUtils.isBetweenLocations(block.getLocation(), plot.getBoundaryA(), plot.getBoundaryB())) {
            e.setCancelled(true);
            BukkitUtils.sendMessage(player, "&cYou cannot place blocks there!");
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (Game.getInstance().isInProgress())
            e.setCancelled(!Game.getInstance().isBuildingPhase());
        else
            e.setCancelled(!BBPlayer.getInstance(e.getPlayer()).hasRestrictionsBypass());

        Setup.handleInteractions(e);
        Game.getInstance().handleInteractions(e);
    }

    // Other
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (Game.getInstance().isInProgress())
            e.setCancelled(!Game.getInstance().isBuildingPhase());
        else
            e.setCancelled(!BBPlayer.getInstance((Player) e.getWhoClicked()).hasRestrictionsBypass());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        e.setCancelled(!BBPlayer.getInstance(e.getPlayer()).hasRestrictionsBypass());
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        e.setCancelled(!BBPlayer.getInstance(player).hasRestrictionsBypass());
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        e.setCancelled(!BBPlayer.getInstance(e.getPlayer()).hasRestrictionsBypass());
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        e.setCancelled(true);
    }
}