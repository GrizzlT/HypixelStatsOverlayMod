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
        StringBuilder message = new StringBuilder("Party includes: ");
        for (String name : HypixelStatsOverlayMod.instance.getPartyManager().getPartyMembers().keySet())
        {
            message.append(name).append(", ");
        }
        sender.addChatMessage(new ChatComponentText(message.toString()));
    }
}
