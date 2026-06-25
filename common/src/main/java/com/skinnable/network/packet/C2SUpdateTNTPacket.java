package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SUpdateTNTPacket(BlockPos pos, int explosionPower)
        implements CustomPacketPayload {

    public static final Type<C2SUpdateTNTPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "update_tnt"));

    public static final StreamCodec<FriendlyByteBuf, C2SUpdateTNTPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SUpdateTNTPacket::pos,
                    ByteBufCodecs.VAR_INT, C2SUpdateTNTPacket::explosionPower,
                    C2SUpdateTNTPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
