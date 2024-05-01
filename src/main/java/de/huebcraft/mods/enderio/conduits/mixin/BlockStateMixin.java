package de.huebcraft.mods.enderio.conduits.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import de.huebcraft.mods.enderio.conduits.block.RedstoneEmitter;
import de.huebcraft.mods.enderio.conduits.block.RedstoneEmittingState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends AbstractBlock.AbstractBlockState implements RedstoneEmittingState {
    protected BlockStateMixin(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
        super(block, propertyMap, codec);
    }

    @Override
    public boolean enderio$emitsRedstone(@NotNull BlockView world, @NotNull BlockPos pos, @Nullable Direction direction) {
        return ((RedstoneEmitter)getBlock()).enderio$emitsRedstone((BlockState) (Object) this, world, pos, direction);
    }
}
