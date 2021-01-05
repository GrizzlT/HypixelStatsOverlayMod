package com.github.grizzlt.hypixelstatsoverlay.commands;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class PartyInspectCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "hpinspectparty";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/hpinspectparty";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        String message = "Party includes: ";
        for (String name : HypixelStatsOverlayMod.partyManager.getPartyMembers())
        {
            message += name + ", ";
        }
        sender.addChatMessage(new ChatComponentText(message));
    }
}
