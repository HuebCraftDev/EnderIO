package de.huebcraft.mods.enderio.conduits.mixin;

import de.huebcraft.mods.enderio.conduits.block.RedstoneEmittingState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin extends BlockView {
    @Redirect(method = "getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;emitsRedstonePower()Z"))
    default boolean redirectEmitsPower(BlockState instance, BlockPos pos, Direction direction, boolean onlyFromGate) {
        return ((RedstoneEmittingState)instance).enderio$emitsRedstone(this, pos, direction);
    }
}
