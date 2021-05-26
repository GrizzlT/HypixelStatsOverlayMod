package com.github.grizzlt.hypixelstatsoverlay.mixin;

import com.github.grizzlt.hypixelstatsoverlay.events.PlayerListUpdateEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin
{
    @Inject(method = "handlePlayerListItem", at = @At("HEAD"))
    public void onProcessPacket(S38PacketPlayerListItem packetIn, CallbackInfo ci)
    {
        if (packetIn.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER || packetIn.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER)
        {
            MinecraftForge.EVENT_BUS.post(new PlayerListUpdateEvent());
        }
    }
}
