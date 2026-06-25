package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2COpenTNTScreenPacket(BlockPos pos, int explosionPower)
        implements CustomPacketPayload {

    public static final Type<S2COpenTNTScreenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "open_tnt_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2COpenTNTScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, S2COpenTNTScreenPacket::pos,
                    ByteBufCodecs.VAR_INT, S2COpenTNTScreenPacket::explosionPower,
                    S2COpenTNTScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
