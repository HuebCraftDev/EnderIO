package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import de.huebcraft.mods.enderio.conduits.lang.ConduitTranslationKeys
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

data object ConduitItems : Registrar<Item>(Registries.ITEM) {
    val CREATIVE_ENDERFACE_CONDUIT = register("enderface_conduits") {
        @Suppress("USELESS_CAST")
        object : Item(FabricItemSettings()) {
            override fun hasGlint(stack: ItemStack?): Boolean = true
        } as Item
    }
    val CONDUITS = mutableListOf<() -> ConduitBlockItem>()
    val CONDUIT_GROUP = FabricItemGroup.builder().displayName(Text.translatable(ConduitTranslationKeys.ITEM_GROUP))
        .entries { _, entries ->
            entries.addAll(CONDUITS.map { it().defaultStack })
        }.icon { CREATIVE_ENDERFACE_CONDUIT().defaultStack }.build()

    val ENERGY = registerConduitItem(EnderConduitTypes.ENERGY, "energy")
    val REDSTONE = registerConduitItem(EnderConduitTypes.REDSTONE, "redstone")
    val ITEM = registerConduitItem(EnderConduitTypes.ITEM, "item")
    val FLUID = registerConduitItem(EnderConduitTypes.FLUID1, "fluid")
    val PRESSURIZED_FLUID = registerConduitItem(EnderConduitTypes.FLUID2, "pressurized_fluid")
    val ENDER_FLUID = registerConduitItem(EnderConduitTypes.FLUID3, "ender_fluid")


    private fun registerConduitItem(type: () -> IConduitType<*>, name: String): () -> ConduitBlockItem {
        val itemSupplier = register(
            name + "_conduit"
        ) { ConduitBlockItem(type, ConduitBlocks.CONDUIT(), FabricItemSettings()) }
        CONDUITS.add(itemSupplier)
        return itemSupplier
    }


    override fun register() {
        super.register()
        Registry.register(Registries.ITEM_GROUP, Identifier(BuildConstants.modId, "conduits"), CONDUIT_GROUP)
    }
}