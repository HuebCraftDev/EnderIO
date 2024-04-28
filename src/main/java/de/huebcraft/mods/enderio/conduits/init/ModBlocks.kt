package de.huebcraft.mods.enderio.conduits.init

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.ConduitBlock
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.registry.Registries

object ModBlocks : BlockRegistrar(Registries.BLOCK) {
    val CONDUIT = register("conduit", false) {
        ConduitBlock(FabricBlockSettings.create().strength(1.5f, 10f).dropsNothing().nonOpaque().dynamicBounds().pistonBehavior(PistonBehavior.BLOCK))
    }
}