package com.jimmy.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executor;

/**
 * Created by Jimmy on 2017/8/4.
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    private Executor executor = null;
    private static Logger logger = LoggerFactory.getLogger(TimeServerHandler.class);

    public TimeServerHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executor.execute(() -> {
            try {
                ctx.writeAndFlush(processRequest(ctx, msg));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ByteBuf processRequest(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        logger.info("Server receive msg:{}", body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? System.currentTimeMillis() + "\n" :
                "BAD ORDER\n";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        Thread.sleep(5 * 1000); //假设业务处理要
        return resp;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("Server is alive");
    }
}
