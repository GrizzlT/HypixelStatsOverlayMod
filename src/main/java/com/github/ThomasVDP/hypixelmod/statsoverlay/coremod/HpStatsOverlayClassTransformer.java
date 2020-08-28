package com.github.ThomasVDP.hypixelmod.statsoverlay.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

public class HpStatsOverlayClassTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        //maybe change labymod tab list

        return basicClass;
    }
}
