package com.skinnable.fabric;

import com.skinnable.Skinnable;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.network.packet.C2SSaveCommandBlockPacket;
import com.skinnable.network.packet.C2SSetCamouflagePacket;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import com.skinnable.network.packet.S2COpenCommandBlockScreenPacket;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import com.skinnable.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;

public class SkinnableFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Skinnable.init();

        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable"));
        CreativeModeTabEvents.modifyOutputEvent(tabKey).register(output -> {
            output.prepend(ModItems.SKINNABLE_SPAWNER.get());
            output.prepend(ModItems.SKINNABLE_COMMAND_BLOCK.get());
        });

        // Serverbound packets
        PayloadTypeRegistry.serverboundPlay().register(C2SSetCamouflagePacket.TYPE, C2SSetCamouflagePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SUpdateSpawnerPacket.TYPE, C2SUpdateSpawnerPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SSaveCommandBlockPacket.TYPE, C2SSaveCommandBlockPacket.STREAM_CODEC);

        // Clientbound packets
        PayloadTypeRegistry.clientboundPlay().register(S2COpenSpawnerScreenPacket.TYPE, S2COpenSpawnerScreenPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2COpenCommandBlockScreenPacket.TYPE, S2COpenCommandBlockScreenPacket.STREAM_CODEC);

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

        ServerPlayNetworking.registerGlobalReceiver(C2SSaveCommandBlockPacket.TYPE, (packet, context) ->
            context.server().execute(() -> {
                if (!context.player().getAbilities().instabuild) return;
                var be = context.player().level().getBlockEntity(packet.pos());
                if (be instanceof SkinnableCommandBlockEntity cmdBe) {
                    SkinnableCommandBlockEntity.Mode mode = SkinnableCommandBlockEntity.Mode.valueOf(packet.mode());
                    cmdBe.setMode(mode);
                    cmdBe.getCommandBlock().setCommand(packet.command());
                    cmdBe.setChanged();
                    if (cmdBe.getLevel() instanceof ServerLevel sl) cmdBe.getCommandBlock().onUpdated(sl);
                }
            })
        );
    }
}
