package me.spaff.buildbattle.server.game;

import me.spaff.buildbattle.Constants;
import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Setup {
    private Player player;

    private Location waitingLobby;
    private Location plotBoundaryA;
    private Location plotBoundaryB;

    private List<Plot> plots = new ArrayList<>();

    public Setup(Player player) {
        this.player = player;
        BBPlayer.getInstance(player).setSetup(this);
    }

    public void start() {
        Game game = Game.getInstance();
        if (game.isInProgress()) {
            BukkitUtils.sendMessage(player, "&cCannot start a setup! the game is in progress!");
            return;
        }
        else {
            Bukkit.getOnlinePlayers().forEach(pl -> {
                Main.getServerManager().sendToRandomHub(pl);
            });
        }

        game.changeState(GameState.NONE);

        player.getInventory().setItem(0, Constants.SETUP_PLOT_ITEM);
        player.getInventory().setItem(1, Constants.SETUP_WAITING_LOBBY_ITEM);
    }

    public void end() {
        if (waitingLobby == null) {
            BukkitUtils.sendMessage(player, "&cWaiting lobby was not set!");
            return;
        }
        if (plots.size() < Constants.MAX_PLAYERS) {
            BukkitUtils.sendMessage(player, "&cNot all plots where added (" + plots.size() + "/" + Constants.MAX_PLAYERS + ")!");
            return;
        }

        new FileManager("arena").save(); // TODO: Add option to create file in FileUtils

        // Save waiting location
        BukkitUtils.sendMessage(player, "&aSaving waiting lobby location...");
        FileUtils.saveLocationToFile(waitingLobby, "arena", "waiting-location");

        // Save plots
        BukkitUtils.sendMessage(player, "&aSaving plots...");
        int index = 1;
        for (Plot plot : plots) {
            FileUtils.saveLocationToFile(plot.getBoundaryA(), "arena", "plots." + index + ".a");
            FileUtils.saveLocationToFile(plot.getBoundaryB(), "arena", "plots." + index + ".b");
            index++;
        }

        Game.getInstance().changeState(GameState.WAITING);

        // Clean up
        BBPlayer.getInstance(player).setSetup(null);
        player.getInventory().clear();

        BukkitUtils.sendMessage(player, "&aArena was set up successfully!");
    }

    public static void handleInteractions(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack useItem = e.getItem();
        if (useItem == null) return;
        if (!Game.getInstance().getState().equals(GameState.NONE)) return;

        BBPlayer bbPlayer = BBPlayer.getInstance(player);
        if (!bbPlayer.isSettingUpGame()) return;

        String useItemDisplayName = useItem.getItemMeta().getDisplayName();

        if (useItemDisplayName.contains(Constants.SETUP_PLOT_ITEM.getItemMeta().getDisplayName())) {
            e.setCancelled(true);

            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                bbPlayer.getSetup().setPlotBoundaryB(player.getLocation());
                BukkitUtils.sendMessage(player, "&aPlot boundary B was set.");
            }
            if (e.getAction().equals(Action.LEFT_CLICK_AIR)) {
                bbPlayer.getSetup().setPlotBoundaryA(player.getLocation());
                BukkitUtils.sendMessage(player, "&aPlot boundary A was set.");
            }

            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                bbPlayer.getSetup().setPlotBoundaryB(e.getClickedBlock().getLocation());
                BukkitUtils.sendMessage(player, "&aPlot boundary B was set.");
            }
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                bbPlayer.getSetup().setPlotBoundaryA(e.getClickedBlock().getLocation());
                BukkitUtils.sendMessage(player, "&aPlot boundary A was set.");
            }
        }

        if (useItemDisplayName.contains(Constants.SETUP_WAITING_LOBBY_ITEM.getItemMeta().getDisplayName())) {
            e.setCancelled(true);
            bbPlayer.getSetup().setWaitingLobby(player.getLocation());
            BukkitUtils.sendMessage(player, "&aWaiting lobby location was set.");
        }
    }

    public void cancel() {
        BBPlayer.getInstance(player).setSetup(null);
    }

    public void setWaitingLobby(Location waitingLobby) {
        this.waitingLobby = waitingLobby;
    }

    public Location getWaitingLobby() {
        return waitingLobby;
    }

    public Location getPlotBoundaryA() {
        return plotBoundaryA;
    }

    public void setPlotBoundaryA(Location plotBoundaryA) {
        this.plotBoundaryA = plotBoundaryA;
    }

    public Location getPlotBoundaryB() {
        return plotBoundaryB;
    }

    public void setPlotBoundaryB(Location plotBoundaryB) {
        this.plotBoundaryB = plotBoundaryB;
    }

    public void setPlot(int index, Plot plot) {
        if (index >= plots.size() || index < 0) {
            BukkitUtils.sendMessage(player, "&cPlot with index " + index + " does not exist!");
            return;
        }
        plots.set(index, plot);
    }

    public void addPlot(Plot plot) {
        if (plots.size() >= Constants.MAX_PLAYERS) {
            BukkitUtils.sendMessage(player, "&cCannot add any more plots! (" + plots.size() + "/" + Constants.MAX_PLAYERS + ")");
            return;
        }
        plots.add(plot);

        plotBoundaryA = null;
        plotBoundaryB = null;
    }

    public List<Plot> getPlots() {
        return plots;
    }
}
