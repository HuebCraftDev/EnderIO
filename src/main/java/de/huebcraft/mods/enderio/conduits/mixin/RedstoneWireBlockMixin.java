package de.huebcraft.mods.enderio.conduits.mixin;

import de.huebcraft.mods.enderio.conduits.block.RedstoneEmittingState;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;)Z", ordinal = 0))
    public boolean redirectConnectTo0(BlockState state, BlockView world, BlockPos pos, Direction direction, boolean bl) {
        BlockPos other = pos.offset(direction);
        return ((RedstoneEmittingState)state).enderio$emitsRedstone(world, other, direction);
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;)Z", ordinal = 1))
    public boolean redirectConnectTo1(BlockState state, BlockView world, BlockPos pos, Direction direction, boolean bl) {
        BlockPos other = pos.offset(direction);
        return ((RedstoneEmittingState)state).enderio$emitsRedstone(world, other, direction);
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"))
    public boolean redirectConnectTo2(BlockState state, Direction dir, BlockView world, BlockPos pos, Direction direction, boolean bl) {
        BlockPos other = pos.offset(direction);
        return ((RedstoneEmittingState)state).enderio$emitsRedstone(world, other, direction);
    }
}
