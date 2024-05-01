package de.huebcraft.mods.enderio.conduits.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public interface RedstoneEmittingState {
    boolean enderio$emitsRedstone(BlockView world, BlockPos pos, @Nullable Direction direction);
}
