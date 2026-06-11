package com.skinnable.network.packet;

import com.skinnable.Skinnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record C2SSetCamouflagePacket(BlockPos pos, BlockState camouflage)
        implements CustomPacketPayload {

    public static final Type<C2SSetCamouflagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Skinnable.MOD_ID, "set_camouflage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetCamouflagePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SSetCamouflagePacket::pos,
                    ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), C2SSetCamouflagePacket::camouflage,
                    C2SSetCamouflagePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
