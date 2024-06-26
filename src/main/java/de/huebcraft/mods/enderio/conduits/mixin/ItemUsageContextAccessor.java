package de.huebcraft.mods.enderio.conduits.mixin;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemUsageContext.class)
public interface ItemUsageContextAccessor {
    @Invoker("getHitResult")
    BlockHitResult invokeGetHitResult();
}
