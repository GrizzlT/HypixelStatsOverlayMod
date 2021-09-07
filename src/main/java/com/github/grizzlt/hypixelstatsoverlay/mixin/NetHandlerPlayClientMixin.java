package com.github.grizzlt.hypixelstatsoverlay.mixin;

import com.github.grizzlt.hypixelapimod.api.error.PublicAPIKeyMissingException;
import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerChangedServerWorldEvent;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerListUpdateEvent;
import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin
{
    private Disposable prevRequest = null;

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
        if (!ServerBasedRegisterUtil.connectedToServer) return;

        if (this.prevRequest != null && !this.prevRequest.isDisposed()) {
            this.prevRequest.dispose();
        }

        System.out.println("Sent status request!");
        this.prevRequest = Mono.fromFuture(HypixelStatsOverlayMod.instance.getHypixelApiMod().handleHypixelAPIRequest(api -> api.getStatus(Minecraft.getMinecraft().thePlayer.getUniqueID())))
                .doOnError(PublicAPIKeyMissingException.class, e -> {
                    System.out.println("API-Key was not set!");
                    Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cDon't forget to set your api key!! Run §6/hpapiquickstart§r§c once.")));
                }).subscribe(statusReply -> {
                    System.out.println("Status request came back!");
                    Minecraft.getMinecraft().addScheduledTask(() -> MinecraftForge.EVENT_BUS.post(new PlayerChangedServerWorldEvent(statusReply)));
                });
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
