package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import team.reborn.energy.api.EnergyStorage

object ModBlockEntities : Registrar<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {
    val CONDUIT_BLOCK_ENTITY = register("conduit") {
        FabricBlockEntityTypeBuilder.create(::ConduitBlockEntity, ConduitBlocks.CONDUIT()).build()
    }

    override fun register() {
        super.register()
        registerLookup()
    }

    @Suppress("UnstableApiUsage")
    private fun registerLookup() {
        EnergyStorage.SIDED.registerForBlockEntity(
            ConduitBlockEntity.createConduitLookup(EnergyStorage.SIDED), CONDUIT_BLOCK_ENTITY()
        )
        ItemStorage.SIDED.registerForBlockEntity(
            ConduitBlockEntity.createConduitLookup(ItemStorage.SIDED), CONDUIT_BLOCK_ENTITY()
        )
        FluidStorage.SIDED.registerForBlockEntity(
            ConduitBlockEntity.createConduitLookup(FluidStorage.SIDED), CONDUIT_BLOCK_ENTITY()
        )
    }
}