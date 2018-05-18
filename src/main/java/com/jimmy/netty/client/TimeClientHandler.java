package com.jimmy.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Jimmy on 2017/8/4.
 */
public class TimeClientHandler extends ChannelHandlerAdapter {
    private CallBack callBack;

    public static interface CallBack {
        void onSuccess(ByteBuf msg) throws ExecutionException, InterruptedException, UnsupportedEncodingException;
    }

    public TimeClientHandler(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        callBack.onSuccess((ByteBuf) msg);
    }

}
