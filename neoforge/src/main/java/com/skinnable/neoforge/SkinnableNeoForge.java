package com.skinnable.neoforge;

import com.skinnable.Skinnable;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.client.renderer.CamouflageBlockEntityRenderer;
import com.skinnable.client.screen.SkinnableSpawnerScreen;
import com.skinnable.network.packet.C2SSetCamouflagePacket;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import com.skinnable.neoforge.platform.NeoForgePlatformHelper;
import com.skinnable.platform.Services;
import com.skinnable.registry.ModBlockEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Skinnable.MOD_ID)
public class SkinnableNeoForge {

    public SkinnableNeoForge(IEventBus modBus) {
        Skinnable.init();
        if (Services.PLATFORM instanceof NeoForgePlatformHelper helper) {
            helper.registerToBus(modBus);
        }

        modBus.addListener(this::registerPackets);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::registerRenderers);
        }
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                C2SSetCamouflagePacket.TYPE,
                C2SSetCamouflagePacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> {
                    if (!ctx.player().getAbilities().instabuild) return;
                    var be = ctx.player().level().getBlockEntity(packet.pos());
                    if (be instanceof ICamouflageBlockEntity camo) camo.setCamouflage(packet.camouflage());
                })
        );

        registrar.playToServer(
                C2SUpdateSpawnerPacket.TYPE,
                C2SUpdateSpawnerPacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> {
                    if (!ctx.player().getAbilities().instabuild) return;
                    var be = ctx.player().level().getBlockEntity(packet.pos());
                    if (be instanceof SkinnableSpawnerBlockEntity spawner) {
                        spawner.setSpawnSettings(packet.entries(), packet.minDelay(), packet.maxDelay(),
                                packet.spawnCount(), packet.maxNearby(), packet.playerRange());
                    }
                })
        );

        registrar.playToClient(
                S2COpenSpawnerScreenPacket.TYPE,
                S2COpenSpawnerScreenPacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                    net.minecraft.client.Minecraft.getInstance().setScreen(
                        new SkinnableSpawnerScreen(packet.pos())
                    )
                )
        );
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(),
                CamouflageBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SKINNABLE_SPAWNER.get(),
                CamouflageBlockEntityRenderer::new);
    }
}
