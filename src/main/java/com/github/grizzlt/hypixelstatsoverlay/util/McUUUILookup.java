package com.github.grizzlt.hypixelstatsoverlay.util;

import com.github.grizzlt.shadowedLibs.org.apache.http.HttpEntity;
import com.github.grizzlt.shadowedLibs.org.apache.http.ParseException;
import com.github.grizzlt.shadowedLibs.org.apache.http.client.HttpClient;
import com.github.grizzlt.shadowedLibs.org.apache.http.client.methods.HttpGet;
import com.github.grizzlt.shadowedLibs.org.apache.http.entity.ContentType;
import com.github.grizzlt.shadowedLibs.org.apache.http.impl.client.HttpClientBuilder;
import com.github.grizzlt.shadowedLibs.org.apache.http.protocol.HTTP;
import com.github.grizzlt.shadowedLibs.org.apache.http.util.Args;
import com.github.grizzlt.shadowedLibs.org.apache.http.util.CharArrayBuffer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class McUUUILookup
{
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final HttpClient httpClient = HttpClientBuilder.create().build();
    private static final JsonParser parser = new JsonParser();

    public static CompletableFuture<UUID> getUuidMono(String playerName)
    {
        CompletableFuture<UUID> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try
            {
                String uuid = (String)httpClient.execute(new HttpGet("https://playerdb.co/api/player/minecraft/" + playerName), reply -> {
                    String content = toString(reply.getEntity(), Charset.defaultCharset());
                    JsonElement parsed = parser.parse(content);
                    if (parsed.getAsJsonObject().has("code") && !parsed.getAsJsonObject().get("code").getAsString().equals("player.found"))
                    {
                        System.out.println("ERROR: couldn't get user uuid!");
                        return "";
                    }
                    return parsed.getAsJsonObject().get("data").getAsJsonObject().get("player").getAsJsonObject().get("id").getAsString();
                });
                future.complete(UUID.fromString(uuid));
            } catch (IOException e)
            {
                System.out.println("ERROR whilst retrieving uuid for " + playerName);
                e.printStackTrace();
            }
        });

        return future;
    }

    private static String toString(final HttpEntity entity, final Charset defaultCharset) throws IOException, ParseException
    {
        Args.notNull(entity, "Entity");
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
            Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
                    "HTTP entity too large to be buffered in memory");
            int i = (int)entity.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            Charset charset = null;
            try {
                final ContentType contentType = ContentType.get(entity);
                if (contentType != null) {
                    charset = contentType.getCharset();
                }
            } catch (final UnsupportedCharsetException ex) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
            if (charset == null) {
                charset = defaultCharset;
            }
            if (charset == null) {
                charset = HTTP.DEF_CONTENT_CHARSET;
            }
            final Reader reader = new InputStreamReader(instream, charset);
            final CharArrayBuffer buffer = new CharArrayBuffer(i);
            final char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } finally {
            instream.close();
        }
    }
}
