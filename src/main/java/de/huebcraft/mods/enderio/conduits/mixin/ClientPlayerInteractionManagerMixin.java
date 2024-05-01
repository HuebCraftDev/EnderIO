package de.huebcraft.mods.enderio.conduits.mixin;

import de.huebcraft.mods.enderio.conduits.block.ConduitBlock;
import de.huebcraft.mods.enderio.conduits.init.ConduitBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "breakBlock", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, World world, BlockState blockState, Block block) {
        if (block == ConduitBlocks.INSTANCE.getCONDUIT().invoke()) {
            if (!((ConduitBlock) block).canBreak(world, pos, blockState, this.client.player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
