package me.spaff.buildbattle.server.game;

@FunctionalInterface
public interface TimerAction {
    void onTimerProgress(int countdown);
}
