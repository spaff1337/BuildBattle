package me.spaff.buildbattle.server.game;

import me.spaff.buildbattle.Constants;
import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.arena.Arena;
import me.spaff.buildbattle.manager.ScoreboardManager;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.buildbattle.utils.Utils;
import me.spaff.spflib.builder.ItemBuilder;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Game {
    private static Game instance;
    private GameState state;
    private GamePhase phase;

    private List<Plot> plots;
    private List<Plot> playerPlots = new ArrayList<>();

    private String theme;
    private Plot currentlyJudgedPlot;
    private BBPlayer currentlyJudgedBuilder;

    private BukkitTask mainTask;
    private int countdown;
    private TimerAction action;

    private Game() {
        boolean wasGameSetup = FileManager.isFileInDirectory(FileManager.getPluginDirectory(), "arena");

        this.state = wasGameSetup ? GameState.WAITING : GameState.NONE;
        this.phase = GamePhase.NONE;
        this.countdown = 0;

        this.plots = new ArrayList<>(Main.getServerManager().getGameData().getPlots());
    }

    public boolean canStart() {
        return isWaiting() && hasMinimumPlayers();
    }

    public boolean hasMinimumPlayers() {
        return Arena.getInstance().getPlayers().size() >= Constants.PLAYERS_REQUIRED_TO_START;
    }

    public boolean hasMaxPlayers() {
        return Arena.getInstance().getPlayers().size() >= Constants.MAX_PLAYERS;
    }

    public void start() {
        if (!canStart()) return;
        changeState(GameState.STARTING);
    }

    public boolean canJoin() {
        return isWaiting() || isStarting() && !hasMaxPlayers();
    }

    public void handleGameCancelling() {
        if (isStarting() && !hasMinimumPlayers()) {
            changeState(GameState.WAITING);
            BukkitUtils.broadcast("&cGame was cancelled! Not enough players to start.");
        }

        if (isInProgress() && Arena.getInstance().getPlayers().size() < 2) {
            onTimerTick(null);
            stopCountdown();

            BukkitUtils.broadcast("");
            BukkitUtils.broadcast("&eGame is cancelled due to low amount of players.");
            BukkitUtils.broadcast("&eSending to hub...");
            BukkitUtils.broadcast("");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        Main.getServerManager().sendToRandomHub(player);
                    });
                    reset();
                }
            }.runTaskLater(Main.getInstance(), 10 * 20);
        }

        ScoreboardManager.updateScoreboards();
    }

    public void changeState(GameState state) {
        this.state = state;
        switch (state) {
            case WAITING:
                handleWaiting();
                break;
            case STARTING:
                handleStarting();
                break;
            case IN_PROGRESS:
                handleProgress();
                break;
            case ENDING:
                handleEnding();
                break;
        }
    }

    private void handleWaiting() {
        onTimerTick(null);
        stopCountdown();
    }

    private void handleStarting() {
        startCountdown(Constants.GAME_START_COUNTDOWN_LENGTH);
        onTimerTick((time) -> {
            if (time > 0) return;
            changeState(GameState.IN_PROGRESS);

            String[] themes = {"Waterfall", "Campfire", "Igloo", "Camp", "Mountain"};
            theme = themes[new Random().nextInt(themes.length - 1)];

            // Teleport players to plots
            Arena.getInstance().getPlayers().forEach(player -> {
                Utils.clearPlayer(player.getBukkitPlayer());
                for (Plot plot : plots) {
                    if (plot.getOwner() != null) continue;

                    plot.setOwner(player);
                    playerPlots.add(plot);

                    player.getBukkitPlayer().teleport(plot.getBottomCenter());
                    break;
                }
            });
        });
    }

    private void handleProgress() {
        startCountdown(Constants.GAME_TIME);
        setPhase(GamePhase.BUILDING);

        ScoreboardManager.updateScoreboards();

        Arena.getInstance().getPlayers().forEach(player -> {
            player.setBypassRestrictions(false);
            player.getBukkitPlayer().setGameMode(GameMode.CREATIVE);
        });

        AtomicInteger voteTimer = new AtomicInteger();
        List<Plot> judgedPlots = new ArrayList<>();

        onTimerTick((time) -> {
            if (time <= 0) {
                stopCountdown();
                if (isBuildingPhase()) {
                    setPhase(GamePhase.VOTING);
                    startCountdown(300);

                    Utils.clearPlayers();
                }
            }
            else {
                Arena.getInstance().getPlayers().forEach(bbplayer -> {
                    Player player = bbplayer.getBukkitPlayer();
                    Plot plotToCheck = null;

                    if (isBuildingPhase()) {
                        for (Plot plot : playerPlots) {
                            if (!plot.getOwner().equals(bbplayer)) continue;
                            plotToCheck = plot;
                        }
                    }
                    else if (isVotingPhase()) {
                        plotToCheck = currentlyJudgedPlot;
                    }

                    if (plotToCheck == null) return;

                    Location boundaryA = plotToCheck.getBoundaryA();
                    Location boundaryB = plotToCheck.getBoundaryB();

                    if (!BukkitUtils.isBetweenLocations(player.getLocation(), boundaryA, boundaryB)) {
                        player.teleport(plotToCheck.getCenter());
                        BukkitUtils.sendMessage(player, "&cYou can't leave the plot!");
                    }
                });

                if (!isVotingPhase()) return;

                if (voteTimer.get() >= Constants.TIME_TO_VOTE) {
                    voteTimer.set(0);

                    // Get votes from previous plot and add score
                    if (currentlyJudgedPlot != null) {
                        Arena.getInstance().getPlayers().forEach(player -> {
                            Vote vote = player.getVote();
                            if (vote.equals(Vote.NONE))
                                vote = Vote.GOOD;

                            currentlyJudgedPlot.addScore(vote.getScore());
                            player.setVote(Vote.NONE);
                        });
                    }

                    giveVotingItems();

                    BBPlayer plotBuilder = null;
                    Plot playerPlot = null;

                    for (Plot plot : playerPlots) {
                        if (judgedPlots.contains(plot)) continue;

                        plotBuilder = plot.getOwner();
                        playerPlot = plot;

                        judgedPlots.add(plot);
                        break;
                    }

                    if (playerPlot == null) {
                        changeState(GameState.ENDING);
                        return;
                    }

                    Plot finalPlayerPlot = playerPlot;
                    Arena.getInstance().getPlayers().forEach(player -> {
                        player.getBukkitPlayer().teleport(finalPlayerPlot.getCenter());
                    });

                    currentlyJudgedPlot = playerPlot;
                    currentlyJudgedBuilder = plotBuilder;
                }
                else
                    voteTimer.getAndIncrement();
            }
        });
    }

    private void handleEnding() {
        onTimerTick(null);
        setPhase(GamePhase.NONE);
        stopCountdown();

        Utils.clearPlayers();
        sendGameSummary();

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    Main.getServerManager().sendToRandomHub(player);
                });
                reset();
            }
        }.runTaskLater(Main.getInstance(), 20 * 10);
    }

    private void sendGameSummary() {
        Map<BBPlayer, Integer> scores = new HashMap<>();
        for (Plot plot : playerPlots) {
            scores.put(plot.getOwner(), plot.getScore());
        }
        // TODO: Sort from highest to lowest score

        BukkitUtils.broadcast("");
        BukkitUtils.broadcast("");

        for (var summary : scores.entrySet()) {
            BBPlayer builder = summary.getKey();
            int score = summary.getValue();

            BukkitUtils.broadcast("&7" + builder.getBukkitPlayer().getName() + " &fScored: &e" + score);
        }

        BukkitUtils.broadcast("");
        BukkitUtils.broadcast("");
    }

    public void startCountdown(int timeSeconds) {
        if (mainTask != null && !mainTask.isCancelled())
            mainTask.cancel();

        countdown = Math.max(0, timeSeconds);

        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mainTask == null || mainTask.isCancelled())
                    return;

                if (countdown <= 0) {
                    stopCountdown();
                    action.onTimerProgress(0);
                    return;
                }

                ScoreboardManager.updateScoreboards();
                action.onTimerProgress(countdown);

                countdown--;
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public void stopCountdown() {
        if (mainTask != null)
            mainTask.cancel();
        mainTask = null;
        countdown = 0;
    }

    public void reset() {
        plots.forEach(plot -> {
            plot.setOwner(null);
            plot.setScore(0);
        });

        clearInstance();
        Arena.clearInstance();

        changeState(GameState.WAITING);
    }

    public void handleInteractions(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        if (!isInProgress() && !isVotingPhase()) return;
        if (currentlyJudgedBuilder == null || currentlyJudgedPlot == null) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        if (hotbarSlot > 4) return;

        BBPlayer bbPlayer = BBPlayer.getInstance(player);
        if (currentlyJudgedBuilder.equals(bbPlayer)) {
            BukkitUtils.sendMessage(player, "&cYou can't vote on your build!");
            return;
        }

        for (Vote vote : Vote.values()) {
            if (vote.ordinal() == 0 || (vote.ordinal() - 1) != hotbarSlot) continue;
            if (bbPlayer.getVote().equals(vote)) return;

            bbPlayer.setVote(vote);
            BukkitUtils.sendMessage(player, "&aYour vote: " + vote.getColor() + vote.getName());
            break;
        }
    }

    private void giveVotingItems() {
        Arena.getInstance().getPlayers().forEach(player -> {
            player.getBukkitPlayer().getInventory().setItem(0, new ItemBuilder.Builder(Material.RED_CONCRETE)
                    .name(Vote.BAD.getColor() + Vote.BAD.getName()).build().getItem());

            player.getBukkitPlayer().getInventory().setItem(1, new ItemBuilder.Builder(Material.GREEN_CONCRETE)
                    .name(Vote.GOOD.getColor() + Vote.GOOD.getName()).build().getItem());

            player.getBukkitPlayer().getInventory().setItem(2, new ItemBuilder.Builder(Material.LIME_CONCRETE)
                    .name(Vote.VERY_GOOD.getColor() + Vote.VERY_GOOD.getName()).build().getItem());

            player.getBukkitPlayer().getInventory().setItem(3, new ItemBuilder.Builder(Material.PURPLE_CONCRETE)
                    .name(Vote.EPIC.getColor() + Vote.EPIC.getName()).build().getItem());

            player.getBukkitPlayer().getInventory().setItem(4, new ItemBuilder.Builder(Material.YELLOW_CONCRETE)
                    .name(Vote.LEGENDARY.getColor() + Vote.LEGENDARY.getName()).build().getItem());
        });
    }

    public Plot getPlayerPlot(BBPlayer player) {
        for (Plot plot : playerPlots) {
            if (plot.getOwner().equals(player))
                return plot;
        }
        return null;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public boolean isBuildingPhase() {
        return getPhase().equals(GamePhase.BUILDING);
    }

    public boolean isVotingPhase() {
        return getPhase().equals(GamePhase.VOTING);
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public String getTheme() {
        return theme;
    }

    public Plot getCurrentlyJudgedPlot() {
        return currentlyJudgedPlot;
    }

    public BBPlayer getCurrentlyJudgedBuilder() {
        return currentlyJudgedBuilder;
    }

    private void onTimerTick(TimerAction action) {
        this.action = action;
    }

    public Location getWaitingLobby() {
        return Main.getServerManager().getGameData().getWaitingLocation();
    }

    public GameState getState() {
        return state;
    }

    public boolean isWaiting() {
        return state.equals(GameState.WAITING);
    }

    public boolean isStarting() {
        return state.equals(GameState.STARTING);
    }

    public boolean isInProgress() {
        return state.equals(GameState.IN_PROGRESS);
    }

    public int getCountdown() {
        return countdown;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static Game getInstance() {
        if (instance == null)
            instance = new Game();
        return instance;
    }
}