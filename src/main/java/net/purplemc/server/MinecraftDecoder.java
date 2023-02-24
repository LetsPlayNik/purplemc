package net.purplemc.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    private int nextState = 1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if(!msg.isReadable()) return;
        int length = readVarInt(msg);
        int packetId = readVarInt(msg);
        System.out.println("angekommen");
        System.out.println("Packet ID: " + packetId + "\nLength: " + length);

        if(nextState == 1) {
            int protocolVersion = readVarInt(msg);
            String address = readString(msg);
            int port = msg.readUnsignedShort();
            nextState = readVarInt(msg);
            System.out.println("Protocol Version: " + protocolVersion + "\nAddress: " + address + "\nPort: " + port + "\nNext State: " + nextState);
        } else {
            System.out.println("Logging in... i guess");
            ByteBuf byteBuf = Unpooled.buffer();
            writeUuid(byteBuf, UUID.randomUUID());
            writeString(byteBuf, "Lets_play_Nik");
            writeVarInt(byteBuf, 0);
            ctx.writeAndFlush(byteBuf);
            nextState = 3;
        }
    }

    public void writeVarInt(ByteBuf byteBuf, int value) {
        while(true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                byteBuf.writeByte(value);
                return;
            }
            byteBuf.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }
    }
    public int readVarInt(ByteBuf byteBuf) {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = byteBuf.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) break;
            position += 7;
            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }
        return value;
    }


    public void writeString(ByteBuf byteBuf, CharSequence charSequence) {
        int size = ByteBufUtil.utf8Bytes(charSequence);
        writeVarInt(byteBuf, size);
        ByteBufUtil.writeUtf8(byteBuf, charSequence);
    }
    public String readString(ByteBuf byteBuf) {
        int length = readVarInt(byteBuf);
        String str = byteBuf.toString(byteBuf.readerIndex(), length, StandardCharsets.UTF_8);
        byteBuf.skipBytes(length);
        return str;
    }

    public void writeUuid(ByteBuf byteBuf, UUID uuid) {
        byteBuf.writeInt((int) (uuid.getMostSignificantBits() >> 32));
        byteBuf.writeInt((int) uuid.getMostSignificantBits());
        byteBuf.writeInt((int) (uuid.getLeastSignificantBits() >> 32));
        byteBuf.writeInt((int) uuid.getLeastSignificantBits());
    }
}
