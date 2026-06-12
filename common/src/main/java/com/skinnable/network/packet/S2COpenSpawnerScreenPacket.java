package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import com.skinnable.data.SpawnEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record S2COpenSpawnerScreenPacket(
        BlockPos pos,
        List<SpawnEntry> entries,
        int minDelay, int maxDelay,
        int spawnCount, int maxNearby, int playerRange
) implements CustomPacketPayload {

    public static final Type<S2COpenSpawnerScreenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "open_spawner_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2COpenSpawnerScreenPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        BlockPos.STREAM_CODEC.encode(buf, pkt.pos);
                        buf.writeVarInt(pkt.entries.size());
                        for (SpawnEntry e : pkt.entries) {
                            Identifier.STREAM_CODEC.encode(buf, e.entityType());
                            buf.writeVarInt(e.weight());
                        }
                        buf.writeVarInt(pkt.minDelay);
                        buf.writeVarInt(pkt.maxDelay);
                        buf.writeVarInt(pkt.spawnCount);
                        buf.writeVarInt(pkt.maxNearby);
                        buf.writeVarInt(pkt.playerRange);
                    },
                    buf -> {
                        BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                        int count = buf.readVarInt();
                        List<SpawnEntry> entries = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            Identifier id = Identifier.STREAM_CODEC.decode(buf);
                            int weight = buf.readVarInt();
                            entries.add(new SpawnEntry(id, weight));
                        }
                        int minDelay = buf.readVarInt();
                        int maxDelay = buf.readVarInt();
                        int spawnCount = buf.readVarInt();
                        int maxNearby = buf.readVarInt();
                        int playerRange = buf.readVarInt();
                        return new S2COpenSpawnerScreenPacket(pos, entries, minDelay, maxDelay, spawnCount, maxNearby, playerRange);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
