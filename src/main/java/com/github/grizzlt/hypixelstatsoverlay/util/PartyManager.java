package com.github.grizzlt.hypixelstatsoverlay.util;

import com.github.grizzlt.shadowedLibs.reactor.core.publisher.Mono;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyManager
{
    private final Map<String, UUID> partyMembers = new HashMap<>();
    private String partyLeader = "";

    Pattern playerJoinedPartyPattern = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) joined the party.$");
    Pattern selfJoinedPartyPattern = Pattern.compile("^You have joined (?:\\[[^]]+]\\s)?(?<name>[^\\s]*?)'s party!$");
    Pattern partyingWithPattern = Pattern.compile("^You'll be partying with: (?<members>.*?)$");
    Pattern playerKickOfflinePattern = Pattern.compile("^Kicked (?<names>.*?) because they were offline.$");
    Pattern partyListNamesComma = Pattern.compile("(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?)(?:,|$)");
    Pattern playerLeftByOffline = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) was removed from your party because they disconnected$");
    Pattern playerLeftPartyPattern = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) has left the party.$");
    Pattern kickedFromPartyByPattern = Pattern.compile("^You have been kicked from the party by (?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) $");
    Pattern playerRemovedFromParty = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) has been removed from the party.$");
    Pattern partyDisbandedPattern = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?) has disbanded the party!$");
    Pattern leaderTransferByLeave = Pattern.compile("^The party was transferred to (?:\\[[^]]+]\\s)?(?<to>[^\\s]*?) because (?:\\[[^]]+]\\s)?(?<from>[^\\s]*?) left$");
    Pattern leaderTransferred = Pattern.compile("^The party was transferred to (?:\\[[^]]+]\\s)?(?<to>[^\\s]*?) by (?:\\[[^]]+]\\s)?(?<from>[^\\s]*?)$");
    Pattern promoteToLeader = Pattern.compile("^(?:\\[[^]]+]\\s)?(?<from>[^\\s]*?) has promoted (?:\\[[^]]+]\\s)?(?<to>[^\\s]*?) to Party Leader$");

    Pattern partyLeaderPattern = Pattern.compile("^Party Leader: (?:\\[[^]]+]\\s)?(?<name>[^\\s]*?)\\s?●$");
    Pattern partyModeratorsPattern = Pattern.compile("^Party Moderators: (?<moderators>.*?)$");
    Pattern partyMembersPattern = Pattern.compile("^Party Members: (?<members>.*?)$");
    Pattern partyMemberListNamePattern = Pattern.compile("(?:\\[[^]]+]\\s)?(?<name>[^\\s]*?)\\s●\\s");

    String leaveParty = "You left the party.";
    String partyEmptyStr = "The party was disbanded because all invites expired and the party was empty";
    String partyDisbandedLeaderOffline = "The party was disbanded because the party leader disconnected.";

    Matcher m;

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event)
    {
        String message = event.message.getUnformattedText();

        if (message.equals(leaveParty))
        {
            this.clearParty();
            return;
        } else if (message.equals(partyEmptyStr))
        {
            this.clearParty();
            return;
        } else if (message.equals(partyDisbandedLeaderOffline))
        {
            this.clearParty();
            return;
        }

        m = playerJoinedPartyPattern.matcher(message);
        if (m.find())
        {
            this.addNameToList(m.group("name"));
            if (!this.partyLeader.equals("")) {
                this.addNameToList(Minecraft.getMinecraft().thePlayer.getName());
                this.partyLeader = Minecraft.getMinecraft().thePlayer.getName();
            }
            return;
        }
        m = selfJoinedPartyPattern.matcher(message);
        if (m.find())
        {
            this.addNameToList(m.group("name"));
            this.partyLeader = m.group("name");
            return;
        }
        m = partyingWithPattern.matcher(message);
        if (m.find())
        {
            String names = m.group("members");
            Matcher m2 = partyListNamesComma.matcher(names);
            while (m2.find())
            {
                this.addNameToList(m2.group("name"));
            }
            return;
        }
        m = playerKickOfflinePattern.matcher(message);
        if (m.find())
        {
            String names = m.group("names");
            Matcher m2 = partyListNamesComma.matcher(names);
            while (m2.find())
            {
                this.removeNameFromList(m2.group("name"));
            }
            return;
        }
        m = playerLeftByOffline.matcher(message);
        if (m.find())
        {
            this.removeNameFromList(m.group("name"));
            return;
        }
        m = playerLeftPartyPattern.matcher(message);
        if (m.find())
        {
            this.removeNameFromList(m.group("name"));
            return;
        }
        m = kickedFromPartyByPattern.matcher(message);
        if (m.find())
        {
            this.clearParty();
            return;
        }
        m = playerRemovedFromParty.matcher(message);
        if (m.find())
        {
            this.removeNameFromList(m.group("name"));
            return;
        }
        m = partyDisbandedPattern.matcher(message);
        if (m.find())
        {
            this.clearParty();
            return;
        }
        m = leaderTransferByLeave.matcher(message);
        if (m.find())
        {
            this.removeNameFromList(m.group("from"));
            this.addNameToList(m.group("to"));
            this.partyLeader = m.group("to");
            return;
        }
        m = leaderTransferred.matcher(message);
        if (m.find())
        {
            this.addNameToList(m.group("from"));
            this.addNameToList(m.group("to"));
            this.partyLeader = m.group("to");
            return;
        }
        m = promoteToLeader.matcher(message);
        if (m.find())
        {
            this.addNameToList(m.group("from"));
            this.addNameToList(m.group("to"));
            this.partyLeader = m.group("to");
            return;
        }
        m = partyLeaderPattern.matcher(message);
        if (m.find())
        {
            this.addNameToList(m.group("name"));
            this.partyLeader = m.group("name");
            return;
        }
        m = partyModeratorsPattern.matcher(message);
        if (m.find())
        {
            String names = m.group("moderators");
            Matcher m2 = partyMemberListNamePattern.matcher(names);
            while (m2.find())
            {
                this.addNameToList(m2.group("name"));
            }
            return;
        }
        m = partyMembersPattern.matcher(message);
        if (m.find())
        {
            String names = m.group("members");
            Matcher m2 = partyMemberListNamePattern.matcher(names);
            while (m2.find())
            {
                this.addNameToList(m2.group("name"));
            }
        }
    }

    private void addNameToList(String name)
    {
        if (!this.partyMembers.containsKey(name))
        {
            Mono.fromFuture(McUUUILookup.getUuidMono(name))
                    .flatMap(uuid -> Mono.fromRunnable(() -> this.partyMembers.put(name, uuid)))
                    .subscribe();
        }
    }

    private void removeNameFromList(String name)
    {
        this.partyMembers.remove(name);
    }

    public void clearParty()
    {
        this.partyMembers.clear();
        this.partyLeader = "";
    }

    public Tuple<String, UUID> getPartyLeader()
    {
        return new Tuple<>(this.partyLeader, this.partyMembers.get(this.partyLeader));
    }

    public Map<String, UUID> getPartyMembers()
    {
        return this.partyMembers;
    }
}
