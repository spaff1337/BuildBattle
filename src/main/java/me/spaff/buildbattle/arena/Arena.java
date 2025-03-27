package me.spaff.buildbattle.arena;

import me.spaff.buildbattle.server.game.Game;
import me.spaff.buildbattle.manager.ScoreboardManager;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.buildbattle.utils.Utils;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public final class Arena {
    private static Arena instance;
    private Set<BBPlayer> players = new HashSet<>();

    private Arena() {}

    public void join(Player player) {
        if (!canBeJoined()) return;

        BBPlayer bbPlayer = BBPlayer.getInstance(player);
        players.add(bbPlayer);

        Utils.clearPlayer(player);
        player.teleport(Game.getInstance().getWaitingLobby());
        bbPlayer.setBypassRestrictions(false);

        ScoreboardManager.updateScoreboards();

        if (Game.getInstance().canStart())
            Game.getInstance().start();
    }

    public void leave(Player player) {
        if (!BBPlayer.hasInstance(player)) return;
        players.remove(BBPlayer.getInstance(player));

        BBPlayer.getInstance(player).clearInstance();
        ScoreboardManager.clearScoreboard(player);

        ScoreboardManager.updateScoreboards();
        Game.getInstance().handleGameCancelling();
    }

    public boolean canBeJoined() {
        Game game = Game.getInstance();
        return game.isWaiting() || game.isStarting() && !game.hasMaxPlayers();
    }

    public Set<BBPlayer> getPlayers() {
        return players;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static Arena getInstance() {
        if (instance == null)
            instance = new Arena();
        return instance;
    }
}