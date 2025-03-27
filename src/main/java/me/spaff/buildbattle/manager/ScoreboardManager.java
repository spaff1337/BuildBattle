package me.spaff.buildbattle.manager;

import me.spaff.api.scoreboard.SPFScoreboard;
import me.spaff.buildbattle.Constants;
import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.arena.Arena;
import me.spaff.buildbattle.server.game.Game;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.spflib.SPFLib;
import me.spaff.spflib.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ScoreboardManager {
    private static BukkitTask task;
    private static HashMap<UUID, SPFScoreboard> scoreboards = new HashMap<>();

    public static void startUpdating() {
        if (task != null) return;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboards();
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public static void updateScoreboard(Player player) {
        BBPlayer bbPlayer = BBPlayer.getInstance(player);
        if (!Arena.getInstance().getPlayers().contains(bbPlayer))
            return;

        scoreboards.putIfAbsent(player.getUniqueId(), SPFLib.createNEWScoreboard(player));

        SPFScoreboard scoreboard = scoreboards.get(player.getUniqueId());
        scoreboard.updateTitle("&7&lBUILD BATTLE");
        scoreboard.updateNumberFormat("&7");

        // BukkitUtils.getHexColor("#ffd329")
        // BukkitUtils.getHexColor("#FDDC5C")

        Game game = Game.getInstance();
        if (game.isWaiting() || game.isStarting()) {
            updateWaiting(scoreboard);
        }
        else if (game.isInProgress()) {
            if (game.isBuildingPhase()) {
                updateBuildingPhase(scoreboard);
            }
            else if (game.isVotingPhase()) {
                updateVotingPhase(scoreboard);
            }
        }
    }

    public static void updateScoreboards() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            updateScoreboard(player);
        });
    }

    private static void updateWaiting(SPFScoreboard scoreboard) {
        String waitingLine = Game.getInstance().isWaiting() ? ("&7Waiting for players...") : ("&7Starting in: &f" + Game.getInstance().getCountdown() + "s");

        scoreboard.updateLines(List.of(
                "",
                waitingLine,
                "",
                "&7Players: &f" + Arena.getInstance().getPlayers().size() + "&8/&7" + Constants.MAX_PLAYERS,
                "",
                "&7www.example.net"
        ));
    }

    private static void updateVotingPhase(SPFScoreboard scoreboard) {
        Game game = Game.getInstance();

        List<String> lines = new ArrayList<>();
        lines.add("");

        if (game.getCurrentlyJudgedBuilder() != null) {
            lines.add("&7Builder: &f" + game.getCurrentlyJudgedBuilder().getBukkitPlayer().getName());
            lines.add("");
        }
        else {
            lines.add("&7Time to Vote!");
            lines.add("");
        }

        lines.add("&7Theme: &f" + Game.getInstance().getTheme());
        lines.add("&7Players: &f" + Arena.getInstance().getPlayers().size() + "&8/&7" + Constants.MAX_PLAYERS);
        lines.add("");
        lines.add("&7www.example.net");

        scoreboard.updateLines(lines);
    }

    private static void updateBuildingPhase(SPFScoreboard scoreboard) {
        Game game = Game.getInstance();
        scoreboard.updateLines(List.of(
                "",
                "&7Time Left: &f" + StringUtils.formatTime(game.getCountdown()),
                "",
                "&7Theme: &f" + Game.getInstance().getTheme(),
                "&7Players: &f" + Arena.getInstance().getPlayers().size() + "&8/&7" + Constants.MAX_PLAYERS,
                "",
                "&7www.example.net"
        ));
    }

    public static boolean hasScoreboard(Player player) {
        return getScoreboard(player) != null;
    }

    public static SPFScoreboard getScoreboard(Player player) {
        return scoreboards.get(player.getUniqueId());
    }

    public static void clearScoreboard(Player player) {
        if (hasScoreboard(player)) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }
    }
}