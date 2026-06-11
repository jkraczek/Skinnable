package com.skinnable.blockentity;

import com.skinnable.data.SpawnEntry;
import com.skinnable.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkinnableSpawnerBlockEntity extends BlockEntity implements ICamouflageBlockEntity {

    @Nullable
    private BlockState camouflage;
    private List<SpawnEntry> spawnEntries = new ArrayList<>();
    private int spawnDelayMin = 200;
    private int spawnDelayMax = 800;
    private int spawnCount = 4;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnDelay = 200;

    public SkinnableSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SKINNABLE_SPAWNER.get(), pos, blockState);
    }

    @Override
    public @Nullable BlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(BlockState state) {
        this.camouflage = state;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public List<SpawnEntry> getSpawnEntries() { return spawnEntries; }
    public int getSpawnDelayMin() { return spawnDelayMin; }
    public int getSpawnDelayMax() { return spawnDelayMax; }
    public int getSpawnCount() { return spawnCount; }
    public int getMaxNearbyEntities() { return maxNearbyEntities; }
    public int getRequiredPlayerRange() { return requiredPlayerRange; }

    public void setSpawnSettings(List<SpawnEntry> entries, int minDelay, int maxDelay,
                                  int spawnCount, int maxNearby, int playerRange) {
        this.spawnEntries = new ArrayList<>(entries);
        this.spawnDelayMin = minDelay;
        this.spawnDelayMax = maxDelay;
        this.spawnCount = spawnCount;
        this.maxNearbyEntities = maxNearby;
        this.requiredPlayerRange = playerRange;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (camouflage != null) {
            output.store("Camouflage", BlockState.CODEC, camouflage);
        }
        var list = output.childrenList("SpawnEntries");
        for (SpawnEntry entry : spawnEntries) {
            var child = list.addChild();
            child.putString("EntityType", entry.entityType().toString());
            child.putInt("Weight", entry.weight());
        }
        output.putInt("SpawnDelayMin", spawnDelayMin);
        output.putInt("SpawnDelayMax", spawnDelayMax);
        output.putInt("SpawnCount", spawnCount);
        output.putInt("MaxNearbyEntities", maxNearbyEntities);
        output.putInt("RequiredPlayerRange", requiredPlayerRange);
        output.putInt("SpawnDelay", spawnDelay);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Camouflage", BlockState.CODEC).ifPresent(s -> camouflage = s);
        spawnEntries = new ArrayList<>();
        for (var child : input.childrenListOrEmpty("SpawnEntries")) {
            Identifier entityTypeId = Identifier.tryParse(child.getStringOr("EntityType", ""));
            int weight = child.getIntOr("Weight", 0);
            if (entityTypeId != null && weight > 0) {
                spawnEntries.add(new SpawnEntry(entityTypeId, weight));
            }
        }
        spawnDelayMin = input.getIntOr("SpawnDelayMin", spawnDelayMin);
        spawnDelayMax = input.getIntOr("SpawnDelayMax", spawnDelayMax);
        spawnCount = input.getIntOr("SpawnCount", spawnCount);
        maxNearbyEntities = input.getIntOr("MaxNearbyEntities", maxNearbyEntities);
        requiredPlayerRange = input.getIntOr("RequiredPlayerRange", requiredPlayerRange);
        spawnDelay = input.getIntOr("SpawnDelay", spawnDelay);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (camouflage != null) {
            tag.put("Camouflage", NbtUtils.writeBlockState(camouflage));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SkinnableSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (be.requiredPlayerRange > 0) {
            double range = be.requiredPlayerRange;
            boolean hasNearbyPlayer = serverLevel.hasNearbyAlivePlayer(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, range
            );
            if (!hasNearbyPlayer) return;
        }

        be.spawnDelay--;
        if (be.spawnDelay > 0) return;

        int range = Math.max(1, be.spawnDelayMax - be.spawnDelayMin);
        be.spawnDelay = be.spawnDelayMin + serverLevel.getRandom().nextInt(range);

        if (be.spawnEntries.isEmpty()) return;

        Optional<Identifier> picked = pickWeightedRandom(be.spawnEntries, serverLevel.getRandom());
        if (picked.isEmpty()) return;

        Identifier entityTypeId = picked.get();
        var optEntityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityTypeId);
        if (optEntityType.isEmpty()) return;

        var entityType = optEntityType.get();
        AABB checkBox = new AABB(
                pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
        );
        int nearbyCount = serverLevel.getEntities(entityType, checkBox, e -> true).size();
        if (nearbyCount >= be.maxNearbyEntities) return;

        for (int i = 0; i < be.spawnCount; i++) {
            double spawnX = pos.getX() + (serverLevel.getRandom().nextDouble() - serverLevel.getRandom().nextDouble()) * 4 + 0.5;
            double spawnY = pos.getY() + serverLevel.getRandom().nextInt(3) - 1;
            double spawnZ = pos.getZ() + (serverLevel.getRandom().nextDouble() - serverLevel.getRandom().nextDouble()) * 4 + 0.5;
            BlockPos spawnPos = BlockPos.containing(spawnX, spawnY, spawnZ);
            entityType.spawn(serverLevel, spawnPos, EntitySpawnReason.SPAWNER);
        }
        be.setChanged();
    }

    private static Optional<Identifier> pickWeightedRandom(List<SpawnEntry> entries, RandomSource random) {
        int totalWeight = entries.stream().mapToInt(SpawnEntry::weight).sum();
        if (totalWeight <= 0) return Optional.empty();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (SpawnEntry entry : entries) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                return Optional.of(entry.entityType());
            }
        }
        return Optional.of(entries.get(entries.size() - 1).entityType());
    }
}
