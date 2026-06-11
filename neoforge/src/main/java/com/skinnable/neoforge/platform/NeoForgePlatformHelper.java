package com.skinnable.neoforge.platform;

import com.skinnable.Skinnable;
import com.skinnable.platform.IPlatformHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    @Override
    public String getPlatformName() { return "NeoForge"; }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(ResourceKey<Registry<T>> registryKey, String id, Supplier<T> factory) {
        DeferredRegister<T> reg = (DeferredRegister<T>) registers.computeIfAbsent(
                registryKey,
                key -> DeferredRegister.create((ResourceKey<Registry<T>>) key, Skinnable.MOD_ID)
        );
        return reg.register(id, factory);
    }

    public void registerToBus(IEventBus bus) {
        for (DeferredRegister<?> reg : registers.values()) {
            reg.register(bus);
        }
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
