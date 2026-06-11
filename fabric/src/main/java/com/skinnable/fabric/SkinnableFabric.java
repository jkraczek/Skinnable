package com.skinnable.fabric;

import com.skinnable.Skinnable;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.network.packet.C2SSetCamouflagePacket;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SkinnableFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Skinnable.init();

        PayloadTypeRegistry.playC2S().register(C2SSetCamouflagePacket.TYPE, C2SSetCamouflagePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SUpdateSpawnerPacket.TYPE, C2SUpdateSpawnerPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2COpenSpawnerScreenPacket.TYPE, S2COpenSpawnerScreenPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(C2SSetCamouflagePacket.TYPE, (packet, context) ->
            context.server().execute(() -> {
                if (!context.player().getAbilities().instabuild) return;
                var be = context.player().level().getBlockEntity(packet.pos());
                if (be instanceof ICamouflageBlockEntity camo) {
                    camo.setCamouflage(packet.camouflage());
                }
            })
        );

        ServerPlayNetworking.registerGlobalReceiver(C2SUpdateSpawnerPacket.TYPE, (packet, context) ->
            context.server().execute(() -> {
                if (!context.player().getAbilities().instabuild) return;
                var be = context.player().level().getBlockEntity(packet.pos());
                if (be instanceof SkinnableSpawnerBlockEntity spawner) {
                    spawner.setSpawnSettings(packet.entries(), packet.minDelay(), packet.maxDelay(),
                            packet.spawnCount(), packet.maxNearby(), packet.playerRange());
                }
            })
        );
    }
}
