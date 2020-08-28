package com.github.ThomasVDP.hypixelmod.statsoverlay.stats.parser;

import com.github.ThomasVDP.shadowedLibs.net.hypixel.api.reply.AbstractReply;
import com.github.ThomasVDP.shadowedLibs.net.tascalate.concurrent.Promise;

import java.util.function.Consumer;

public class RequestWrapper
{
    private boolean isDone = false;
    private AbstractReply reply;

    public RequestWrapper(Promise<? extends AbstractReply> request, Consumer<RequestWrapper> consumerOnComplete)
    {
        request.thenAcceptAsync(reply -> {
            this.reply = reply;
            consumerOnComplete.accept(this);
            this.isDone = true;
        });
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
