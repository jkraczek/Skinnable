package com.skinnable.fabric.platform;

import com.skinnable.platform.IPlatformHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

import static com.skinnable.Skinnable.MOD_ID;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() { return "Fabric"; }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(ResourceKey<? extends Registry<T>> registryKey, String id, Supplier<T> factory) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, id);
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getOptional(registryKey.identifier())
                .orElseThrow(() -> new IllegalStateException("Unknown registry: " + registryKey.identifier()));
        T value = Registry.register(registry, location, factory.get());
        return () -> value;
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
