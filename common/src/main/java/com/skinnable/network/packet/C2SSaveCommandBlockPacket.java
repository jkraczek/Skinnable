package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SSaveCommandBlockPacket(BlockPos pos, String command, String mode)
        implements CustomPacketPayload {

    public static final Type<C2SSaveCommandBlockPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "save_command_block"));

    public static final StreamCodec<FriendlyByteBuf, C2SSaveCommandBlockPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SSaveCommandBlockPacket::pos,
                    ByteBufCodecs.STRING_UTF8, C2SSaveCommandBlockPacket::command,
                    ByteBufCodecs.STRING_UTF8, C2SSaveCommandBlockPacket::mode,
                    C2SSaveCommandBlockPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
