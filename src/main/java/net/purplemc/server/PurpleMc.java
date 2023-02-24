package net.purplemc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class PurpleMc {

    public void start() {
        EventLoopGroup eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroup);
        serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.addLast("timeout", new ReadTimeoutHandler(30));
                //channelPipeline.addLast("frame-decoder", new FrameDecoder());
                //channelPipeline.addLast("length-encoder", new FrameEncoder());
                channelPipeline.addLast("flow-handler", new FlowControlHandler());
                channelPipeline.addLast("mc-decoder", new MinecraftDecoder());
            }
        });
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            System.out.println("running lol");
            serverBootstrap.bind(25565).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
