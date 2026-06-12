package com.skinnable.neoforge;

import com.skinnable.Skinnable;
import com.skinnable.blockentity.ICamouflageBlockEntity;
import com.skinnable.blockentity.SkinnableCommandBlockEntity;
import com.skinnable.blockentity.SkinnableSpawnerBlockEntity;
import com.skinnable.client.renderer.CamouflageBlockEntityRenderer;
import com.skinnable.client.screen.SkinnableCommandBlockScreen;
import com.skinnable.client.screen.SkinnableSpawnerScreen;
import com.skinnable.network.packet.C2SSaveCommandBlockPacket;
import com.skinnable.network.packet.C2SSetCamouflagePacket;
import com.skinnable.network.packet.C2SUpdateSpawnerPacket;
import com.skinnable.network.packet.S2COpenCommandBlockScreenPacket;
import com.skinnable.network.packet.S2COpenSpawnerScreenPacket;
import com.skinnable.neoforge.platform.NeoForgePlatformHelper;
import com.skinnable.platform.Services;
import com.skinnable.registry.ModBlockEntityTypes;
import com.skinnable.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::buildCreativeTab);
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

        registrar.playToServer(
                C2SSaveCommandBlockPacket.TYPE,
                C2SSaveCommandBlockPacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> {
                    if (!ctx.player().getAbilities().instabuild) return;
                    var be = ctx.player().level().getBlockEntity(packet.pos());
                    if (be instanceof SkinnableCommandBlockEntity cmdBe) {
                        SkinnableCommandBlockEntity.Mode mode = SkinnableCommandBlockEntity.Mode.valueOf(packet.mode());
                        cmdBe.setMode(mode);
                        cmdBe.getCommandBlock().setCommand(packet.command());
                        cmdBe.setChanged();
                        if (cmdBe.getLevel() instanceof ServerLevel sl) cmdBe.getCommandBlock().onUpdated(sl);
                    }
                })
        );

        registrar.playToClient(
                S2COpenSpawnerScreenPacket.TYPE,
                S2COpenSpawnerScreenPacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                    net.minecraft.client.Minecraft.getInstance().setScreen(
                        new SkinnableSpawnerScreen(packet.pos(),
                            packet.entries(), packet.minDelay(), packet.maxDelay(),
                            packet.spawnCount(), packet.maxNearby(), packet.playerRange())
                    )
                )
        );

        registrar.playToClient(
                S2COpenCommandBlockScreenPacket.TYPE,
                S2COpenCommandBlockScreenPacket.STREAM_CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                    net.minecraft.client.Minecraft.getInstance().setScreen(
                        new SkinnableCommandBlockScreen(packet.pos(), packet.command(), packet.mode())
                    )
                )
        );
    }

    private void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Skinnable.MOD_ID, "skinnable"));
        if (event.getTabKey().equals(tabKey)) {
            event.accept(ModItems.SKINNABLE_COMMAND_BLOCK.get());
            event.accept(ModItems.SKINNABLE_SPAWNER.get());
        }
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SKINNABLE_COMMAND_BLOCK.get(),
                CamouflageBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SKINNABLE_SPAWNER.get(),
                CamouflageBlockEntityRenderer::new);
    }
}
