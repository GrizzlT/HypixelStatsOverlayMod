package com.github.grizzlt.hypixelstatsoverlay.commands;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class PartyResetCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "hpresetparty";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/hpresetparty";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        HypixelStatsOverlayMod.partyManager.clearParty();
    }
}
