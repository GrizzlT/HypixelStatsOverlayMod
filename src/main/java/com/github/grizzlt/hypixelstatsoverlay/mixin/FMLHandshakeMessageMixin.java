package com.github.grizzlt.hypixelstatsoverlay.mixin;

import com.github.grizzlt.hypixelstatsoverlay.Reference;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(FMLHandshakeMessage.ModList.class)
public abstract class FMLHandshakeMessageMixin
{
    @Redirect(method = "<init>(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    public <K,V> V onModListInit(Map<K,V> map, K modId, V modVersion)
    {
        if (modId.equals(Reference.MOD_ID) || modId.equals("hypixelapimod")) {
            System.out.println("Skipped sending \"" + modId + "\" to the server!");
            return null;
        }
        return map.put(modId, modVersion);
    }
}
