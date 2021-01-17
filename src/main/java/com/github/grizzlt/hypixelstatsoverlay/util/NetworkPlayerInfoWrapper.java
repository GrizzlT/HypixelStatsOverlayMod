package com.github.grizzlt.hypixelstatsoverlay.util;

import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Objects;

public class NetworkPlayerInfoWrapper
{
    public NetworkPlayerInfo playerInfo;

    public NetworkPlayerInfoWrapper(NetworkPlayerInfo playerInfo)
    {
        this.playerInfo = playerInfo;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkPlayerInfoWrapper that = (NetworkPlayerInfoWrapper) o;
        if (this.playerInfo == null || that.playerInfo == null) return false;
        return Objects.equals(this.playerInfo.getGameProfile(), that.playerInfo.getGameProfile())
                && Objects.equals(this.playerInfo.getGameType(), that.playerInfo.getGameType())
                && Objects.equals(this.playerInfo.getDisplayName(), that.playerInfo.getDisplayName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(playerInfo.getGameProfile() ,playerInfo.getGameType(), playerInfo.getDisplayName());
    }
}
