package com.github.grizzlt.hypixelstatsoverlay.stats.parser;

import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.AbstractReply;
import com.github.grizzlt.shadowedLibs.reactor.core.Disposable;
import com.github.grizzlt.shadowedLibs.reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class RequestWrapper
{
    private boolean isDone = false;
    private AbstractReply reply;
    private Disposable request;

    public RequestWrapper(Mono<? extends AbstractReply> request, Consumer<RequestWrapper> consumerOnComplete)
    {
        this.request = request.flatMap(reply -> Mono.fromRunnable(() -> {
            this.reply = reply;
            consumerOnComplete.accept(this);
            this.isDone = true;
        })).subscribe();
    }

    public void cancel()
    {
        request.dispose();
    }

    public AbstractReply getReply()
    {
        return this.reply;
    }

    public boolean isDone()
    {
        return this.isDone;
    }
}
