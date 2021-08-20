package com.github.grizzlt.hypixelstatsoverlay.stats.cache;

public class LobbyBasedCacheExpiry implements CacheExpiry
{
    private int countDown;
    private final ExpiryType expiryType;

    private boolean inLobbyOrGame;

    public LobbyBasedCacheExpiry(ExpiryType expiryType, int initCountDown, boolean inLobby)
    {
        this.countDown = initCountDown;
        this.expiryType = expiryType;
        this.inLobbyOrGame = (expiryType == ExpiryType.LEAVE_GAME) != inLobby;
    }

    public void onPlayerChangeWorld(boolean toLobby)
    {
        if (expiryType == ExpiryType.LEAVE_GAME) {
            if (inLobbyOrGame) {
                this.countDown--;
            }
            this.inLobbyOrGame = !toLobby;
        } else if (expiryType == ExpiryType.LEAVE_LOBBY) {
            if (inLobbyOrGame) {
                this.countDown--;
            }
            this.inLobbyOrGame = toLobby;
        }
        if (expiryType == ExpiryType.BOTH) {
            this.countDown--;
        } else if (expiryType == ExpiryType.LOBBY && toLobby) {
            this.countDown--;
        } else if (expiryType == ExpiryType.GAME && !toLobby) {
            this.countDown--;
        }
    }

    @Override
    public boolean isExpired()
    {
        return countDown == 0;
    }

    public enum ExpiryType
    {
        LOBBY,
        GAME,
        BOTH,
        LEAVE_GAME,
        LEAVE_LOBBY,
    }
}
