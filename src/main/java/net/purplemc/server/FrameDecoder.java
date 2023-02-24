package net.purplemc.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(!in.isReadable()) return;
        int originalReaderIndex = in.readerIndex();
        for(int i = 0; i < 3; i++) {
            if(!in.isReadable()) {
                in.readerIndex(originalReaderIndex);
                return;
            }
            byte read = in.readByte();
            if(read >= 0) {
                in.readerIndex(originalReaderIndex);
                int packetLength = ByteUtils.readVarInt(in);
                if(in.readableBytes() >= packetLength) {
                    out.add(in.readRetainedSlice(packetLength));
                } else {
                    in.readerIndex(originalReaderIndex);
                }
                return;
            }
        }
        throw new CorruptedFrameException("VarInt too big!");
    }
}
