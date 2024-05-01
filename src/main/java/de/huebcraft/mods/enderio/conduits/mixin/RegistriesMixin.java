package de.huebcraft.mods.enderio.conduits.mixin;

import de.huebcraft.mods.enderio.conduits.conduit.type.ConduitTypeSorter;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class RegistriesMixin {
    @Inject(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registries;validate(Lnet/minecraft/registry/Registry;)V", shift = At.Shift.AFTER))
    private static void afterFreeze(CallbackInfo ci) {
        ConduitTypeSorter.INSTANCE.afterRegistryFreeze();
    }
}
