package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2COpenCommandBlockScreenPacket(BlockPos pos, String command, String mode)
        implements CustomPacketPayload {

    public static final Type<S2COpenCommandBlockScreenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "open_command_block_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2COpenCommandBlockScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, S2COpenCommandBlockScreenPacket::pos,
                    ByteBufCodecs.STRING_UTF8, S2COpenCommandBlockScreenPacket::command,
                    ByteBufCodecs.STRING_UTF8, S2COpenCommandBlockScreenPacket::mode,
                    S2COpenCommandBlockScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
