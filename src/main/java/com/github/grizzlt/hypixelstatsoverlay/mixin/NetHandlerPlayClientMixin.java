package com.github.grizzlt.hypixelstatsoverlay.mixin;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerListUpdateEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin
{
    @Inject(method = "handlePlayerListItem", at = @At("RETURN"))
    public void onHandlePlayerListItem(S38PacketPlayerListItem packetIn, CallbackInfo ci)
    {
        if (packetIn.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER || packetIn.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER)
        {
            MinecraftForge.EVENT_BUS.post(new PlayerListUpdateEvent());
        }
    }

    @Inject(method = "handlePlayerPosLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void onTerrainLoadingOver(S08PacketPlayerPosLook packetIn, CallbackInfo ci)
    {
        HypixelStatsOverlayMod.instance.getGameParsers().onPlayerChangeServerWorld();
    }



    //error fixing
    @Redirect(method = "handleTeams", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;removeTeam(Lnet/minecraft/scoreboard/ScorePlayerTeam;)V"))
    public void removeTeamInTeamsUpdate(Scoreboard scoreboard, ScorePlayerTeam scorePlayerTeam)
    {
        if (scorePlayerTeam == null) {
            HypixelStatsOverlayMod.LOGGER.warn("Trying to remove a team that is unknown to the scoreboard!");
            return;
        }
        scoreboard.removeTeam(scorePlayerTeam);
    }

    @Redirect(method = "handleScoreboardObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;removeObjective(Lnet/minecraft/scoreboard/ScoreObjective;)V"))
    public void removeObjectiveInObjectiveUpdate(Scoreboard scoreboard, ScoreObjective scoreObjective)
    {
        if (scoreObjective == null) {
            HypixelStatsOverlayMod.LOGGER.warn("Trying to remove a scoreobjective that is unknown to the scoreboard!");
            return;
        }
        scoreboard.removeObjective(scoreObjective);
    }
}
