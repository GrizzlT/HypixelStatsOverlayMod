package com.github.ThomasVDP.hypixelmod.statsoverlay.util;

import org.apache.commons.io.Charsets;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BwSniperHaxRequester
{
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private final Pattern pattern  = Pattern.compile("^\\{'sniper': (?<isSniper>.*?), 'report': (?<hax>.*?)}$");

    public CompletableFuture<SniperHaxReply> sendSniperRequest(String playerName)
    {
        CompletableFuture<SniperHaxReply> future = new CompletableFuture<>();

        this.service.submit(() -> {
            try {
                SniperHaxReply result = this.httpClient.execute(new HttpGet("url" + playerName), obj -> { //!!!! no url set!!!!!
                    String content = EntityUtils.toString(obj.getEntity(), Charsets.UTF_8);
                    Matcher m = this.pattern.matcher(content);
                    if (m.find()) {
                        try {
                            return new SniperHaxReply(!m.group("isSniper").equals("False"), Integer.parseInt(m.group("hax")));
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    System.out.println("Failed to get the correct data");
                    return new SniperHaxReply();
                });

                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public static class SniperHaxReply
    {
        public boolean isSniper = false;
        public int hax = -1;

        public SniperHaxReply() {}

        public SniperHaxReply(boolean isSniper, int hax)
        {
            this.isSniper = isSniper;
            this.hax = hax;
        }
    }
}
