package com.github.grizzlt.hypixelstatsoverlay.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyManager
{
    private final List<String> partyMembers = new ArrayList<>();
    //sprivate String partyLeader = "";

    Pattern joinPartyOfPattern = Pattern.compile("^You have joined (?<leader>.*?)'s party!$");
    Pattern joinPartyWithPattern1 = Pattern.compile("^You'll be partying with: (?<members>.*?)$");
    Pattern joinPartyWithPattern2 = Pattern.compile("^(?:(?<member>[^, ]+), )+$");
    Pattern playerJoinedPartyPattern = Pattern.compile("^(?<name>.*?) joined the party.$");
    Pattern playerLeftPartyPattern = Pattern.compile("^(?<name>.*?) has left the party.$");
    Pattern partyTransferredToDisconnectPattern = Pattern.compile("^The party was transferred to (?:.*?) because (?:\\[.*?] )?(?<leader>.*?) left$");
    //Pattern partyTransferredToPattern = Pattern.compile("The party was transferred to (?:\\[.*?] )?(?<leader>.*?) by (?:.*?)$");
    Pattern kickedForOfflinePattern = Pattern.compile("^Kicked (?:\\[.*?] )?(?<member>.*?) because they were offline.$");
    Pattern leftBecauseDisconnectedPattern = Pattern.compile("^(?:\\[.*] )?(?<name>.*?) was removed from your party because they disconnected$");

    Pattern partyLeaderPattern = Pattern.compile("^Party Leader: (?:\\[.*?] )?(?<leader>.*?) ●$");
    Pattern partyModeratorPattern = Pattern.compile("^Party Moderators: (?<names>.*?)$");
    Pattern partyMemberPattern = Pattern.compile("^Party Members: (?<names>.*?)$");
    Pattern memberStatusPattern = Pattern.compile("^(?:(?<name>[^ ]+) ● )*$");

    Matcher m;

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event)
    {
        String message = event.message.getUnformattedText();

        // Case 1: join a party (leader specified)
        this.m = this.joinPartyOfPattern.matcher(message);
        if (this.m.find()) {
            this.addNameToList(Minecraft.getMinecraft().thePlayer.getGameProfile().getName());
            this.addNameToList(m.group("leader"));
            return;
        }

        // Case 2: you left the party or party was disbanded
        if (message.equals("You left the party.")
                || message.equals("The party was disbanded because all invites expired and the party was empty")
                || message.matches("^(.*?) has disbanded the party!$")) {
            this.partyMembers.clear();
            return;
        }

        // Case 3: someone joined the party
        this.m = this.playerJoinedPartyPattern.matcher(message);
        if (m.find()) {
            if (!this.partyMembers.contains(Minecraft.getMinecraft().thePlayer.getGameProfile().getName()))
            {
                this.partyMembers.add(Minecraft.getMinecraft().thePlayer.getGameProfile().getName());
            }
            this.addNameToList(m.group("name"));
            return;
        }

        // Case 4: someone left the party
        this.m = this.playerLeftPartyPattern.matcher(message);
        if (m.find()) {
            this.partyMembers.remove(m.group("name"));
            return;
        }

        // Case 5: join party (party members specified)
        this.m = this.joinPartyWithPattern1.matcher(message);
        if (this.m.find()) {
            String members = m.group("members") + ", ";
            this.m = this.joinPartyWithPattern2.matcher(members);
            while (m.find()) {
                this.addNameToList(m.group("member"));
            }
            return;
        }

        // Case 6: party was transferred (new leader)
        this.m = this.partyTransferredToDisconnectPattern.matcher(message);
        if (this.m.find()) {
            this.partyMembers.add(m.group("leader"));
            return;
        }

        // Case 7: Someone got kicked for being offline
        this.m = this.kickedForOfflinePattern.matcher(message);
        if (this.m.find()) {
            this.partyMembers.remove(m.group("member"));
            return;
        }

        // Case 8: Someone left because disconnected
        this.m = this.leftBecauseDisconnectedPattern.matcher(message);
        if (this.m.find()) {
            this.partyMembers.remove(m.group("name"));
        }

        /*// Case 9: Party was transferred by choice (only when leader is necessary)
        this.m = this.partyTransferredToPattern.matcher(message);
        if (this.m.find()) {
            this.partyMembers.add(m.group("leader"));
        }*/

        // Case 10 - 11 - 12: party leader, party moderator, party members,
        this.m = this.partyLeaderPattern.matcher(message);
        if (this.m.find()) {
            this.partyMembers.add(m.group("leader"));
            return;
        }

        this.m = this.partyModeratorPattern.matcher(message);
        if (this.m.find()) {
            String names = this.m.group("names");
            this.m = this.memberStatusPattern.matcher(names);
            while (this.m.find()) {
                this.partyMembers.add(m.group("name"));
            }
        }

        this.m = this.partyMemberPattern.matcher(message);
        if (this.m.find()) {
            String names = this.m.group("names");
            this.m = this.memberStatusPattern.matcher(names);
            while (this.m.find()) {
                this.partyMembers.add(m.group("name"));
            }
        }
    }

    private void addNameToList(String name)
    {
        if (!this.partyMembers.contains(name))
        {
            this.partyMembers.add(name);
        }
    }

    public void clearParty()
    {
        this.partyMembers.clear();
    }

    /*public String getPartyLeader()
    {
        return this.partyLeader;
    }*/

    public List<String> getPartyMembers()
    {
        return this.partyMembers;
    }
}
