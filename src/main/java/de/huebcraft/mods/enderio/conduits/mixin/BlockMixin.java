package de.huebcraft.mods.enderio.conduits.mixin;

import de.huebcraft.mods.enderio.conduits.block.RedstoneEmitter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements RedstoneEmitter {
    public BlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean enderio$emitsRedstone(@NotNull BlockState state, @NotNull BlockView world, @NotNull BlockPos pos, @Nullable Direction direction) {
        return emitsRedstonePower(state);
    }
}
