package me.spaff.buildbattle.player;

import me.spaff.buildbattle.server.game.Setup;
import me.spaff.buildbattle.server.game.Vote;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BBPlayer {
    private static Set<BBPlayer> players = new HashSet<>();
    private final Player player;

    private Setup setup;
    private boolean bypassRestrictions = false;
    private Vote vote;

    public BBPlayer(Player player) {
        this.player = player;
        this.vote = Vote.NONE;
        players.add(this);
    }

    // Setup
    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public Setup getSetup() {
        return this.setup;
    }

    public boolean isSettingUpGame() {
        return getSetup() != null;
    }

    // Voting
    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }

    public void clearVote() {
        this.vote = null;
    }

    // Bypass
    public boolean hasRestrictionsBypass() {
        return bypassRestrictions;
    }

    public void setBypassRestrictions(boolean bypassRestrictions) {
        this.bypassRestrictions = bypassRestrictions;
    }

    // Other
    private static BBPlayer getPlayer(Player player) {
        for (BBPlayer bbplayer : players) {
            if (bbplayer.getBukkitPlayer().getUniqueId().equals(player.getUniqueId()))
                return bbplayer;
        }
        return null; // new BBPlayer(player)
    }

    public static BBPlayer getInstance(Player player) {
        return !hasInstance(player) ? new BBPlayer(player) : getPlayer(player);
    }

    public static boolean hasInstance(Player player) {
        return getPlayer(player) != null;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public void clearInstance() {
        players.remove(this);
    }

    public static Set<BBPlayer> getBBPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BBPlayer bbPlayer)) return false;
        return bypassRestrictions == bbPlayer.bypassRestrictions && Objects.equals(player, bbPlayer.player) && Objects.equals(setup, bbPlayer.setup) && vote == bbPlayer.vote;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, setup, bypassRestrictions, vote);
    }
}