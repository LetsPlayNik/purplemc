package net.purplemc.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class FrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = ctx.alloc().heapBuffer(5);
        ByteUtils.writeVarInt(byteBuf, msg.readableBytes());
        out.add(byteBuf);
        out.add(byteBuf.retain());
    }
}
