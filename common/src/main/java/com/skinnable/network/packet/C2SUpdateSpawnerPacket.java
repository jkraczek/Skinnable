package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import com.skinnable.data.SpawnEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record C2SUpdateSpawnerPacket(
        BlockPos pos,
        List<SpawnEntry> entries,
        int minDelay, int maxDelay,
        int spawnCount, int maxNearby, int playerRange
) implements CustomPacketPayload {

    public static final Type<C2SUpdateSpawnerPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "update_spawner"));

    private static final StreamCodec<FriendlyByteBuf, SpawnEntry> ENTRY_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SpawnEntry::entityType,
            ByteBufCodecs.VAR_INT, SpawnEntry::weight,
            SpawnEntry::new
    );

    public static final StreamCodec<FriendlyByteBuf, C2SUpdateSpawnerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SUpdateSpawnerPacket::pos,
                    ENTRY_CODEC.apply(ByteBufCodecs.list()), C2SUpdateSpawnerPacket::entries,
                    ByteBufCodecs.VAR_INT, C2SUpdateSpawnerPacket::minDelay,
                    ByteBufCodecs.VAR_INT, C2SUpdateSpawnerPacket::maxDelay,
                    ByteBufCodecs.VAR_INT, C2SUpdateSpawnerPacket::spawnCount,
                    ByteBufCodecs.VAR_INT, C2SUpdateSpawnerPacket::maxNearby,
                    ByteBufCodecs.VAR_INT, C2SUpdateSpawnerPacket::playerRange,
                    C2SUpdateSpawnerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
