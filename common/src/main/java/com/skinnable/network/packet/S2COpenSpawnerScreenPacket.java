package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2COpenSpawnerScreenPacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<S2COpenSpawnerScreenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "open_spawner_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2COpenSpawnerScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, S2COpenSpawnerScreenPacket::pos,
                    S2COpenSpawnerScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
