package com.jimmy.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jimmy on 2017/8/4.
 */
public class TimeClient {
    private static Channel channel = null;
    private static AtomicInteger temp = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(TimeClientHandler.class);
    private ConcurrentHashMap<Integer, CompletableFuture<ByteBuf>> requestQuere = new ConcurrentHashMap<>();

    public void connect(int port, String host) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024), new TimeClientHandler(msg -> {
                                int key = msg.slice(0, 4).readInt();
                                ByteBuf resp = msg.skipBytes(4);
                                requestQuere.remove(key).complete(resp);
                            }));
                        }
                    });
            channel = bootstrap.connect(host, port).sync().channel();
            while (true) send(channel, temp.getAndIncrement());
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new TimeClient().connect(port, "127.0.0.1");
    }

    private void send(Channel channel, int key) throws InterruptedException, ExecutionException, TimeoutException, UnsupportedEncodingException {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        requestQuere.put(key, future);
        byte[] keyByte = int2Byte(key);
        byte[] content = "QUERY TIME ORDER\n".getBytes();
        byte[] req = new byte[keyByte.length + content.length];
        System.arraycopy(keyByte, 0, req, 0, keyByte.length);
        System.arraycopy(content, 0, req, keyByte.length, content.length);
        ByteBuf firstMessage = Unpooled.buffer(content.length);
        firstMessage.writeBytes(req);
        channel.writeAndFlush(firstMessage);
        ByteBuf resp = future.get(100, TimeUnit.SECONDS);
        byte[] response = new byte[resp.readableBytes()];
        resp.readBytes(response);
        String str = new String(response, "UTF-8");
        logger.info("Now is :{}",str);
    }

    private byte[] int2Byte(int key) {
        byte[] intBytes = new byte[4];
        intBytes[0] = (byte) (key >> 24);
        intBytes[1] = (byte) (key >> 16);
        intBytes[2] = (byte) (key >> 8);
        intBytes[3] = (byte) (key >> 0);
        return intBytes;
    }
}
