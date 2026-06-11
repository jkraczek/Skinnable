package com.skinnable.fabric;

import com.skinnable.client.renderer.CamouflageBlockEntityRenderer;
import com.skinnable.client.screen.SkinnableSpawnerScreen;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import com.skinnable.registry.ModBlockEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class SkinnableFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(),
                CamouflageBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntityTypes.SKINNABLE_SPAWNER.get(),
                CamouflageBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(S2COpenSpawnerScreenPacket.TYPE, (packet, context) ->
            context.client().execute(() ->
                context.client().setScreen(new SkinnableSpawnerScreen(packet.pos()))
            )
        );
    }
}
