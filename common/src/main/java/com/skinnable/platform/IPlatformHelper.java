package com.skinnable.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Supplier;

public interface IPlatformHelper {
    String getPlatformName();
    <T> Supplier<T> register(ResourceKey<? extends Registry<T>> registry, String id, Supplier<T> factory);
    void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
