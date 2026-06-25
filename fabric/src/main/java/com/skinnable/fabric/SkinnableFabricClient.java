package com.skinnable.fabric;

import com.skinnable.client.renderer.CamouflageBlockEntityRenderer;
import com.skinnable.client.screen.SkinnableCommandBlockScreen;
import com.skinnable.client.screen.SkinnableSpawnerScreen;
import com.skinnable.client.screen.SkinnableTNTScreen;
import com.skinnable.network.packet.S2COpenCommandBlockScreenPacket;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import com.skinnable.network.packet.S2COpenTNTScreenPacket;
import com.skinnable.registry.ModBlockEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class SkinnableFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(),
                CamouflageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.SKINNABLE_SPAWNER.get(),
                CamouflageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.SKINNABLE_TNT.get(),
                CamouflageBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(S2COpenSpawnerScreenPacket.TYPE, (packet, context) ->
            context.client().execute(() ->
                context.client().setScreen(new SkinnableSpawnerScreen(packet.pos(),
                        packet.entries(), packet.minDelay(), packet.maxDelay(),
                        packet.spawnCount(), packet.maxNearby(), packet.playerRange()))
            )
        );

        ClientPlayNetworking.registerGlobalReceiver(S2COpenCommandBlockScreenPacket.TYPE, (packet, context) ->
            context.client().execute(() ->
                context.client().setScreen(new SkinnableCommandBlockScreen(packet.pos(), packet.command(), packet.mode()))
            )
        );

        ClientPlayNetworking.registerGlobalReceiver(S2COpenTNTScreenPacket.TYPE, (packet, context) ->
            context.client().execute(() ->
                context.client().setScreen(new SkinnableTNTScreen(packet.pos(), packet.explosionPower()))
            )
        );
    }
}
