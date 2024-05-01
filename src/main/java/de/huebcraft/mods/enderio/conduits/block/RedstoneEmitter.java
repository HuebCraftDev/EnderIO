package de.huebcraft.mods.enderio.conduits.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public interface RedstoneEmitter {
    boolean enderio$emitsRedstone(BlockState state, BlockView world, BlockPos pos, @Nullable Direction direction);
}
