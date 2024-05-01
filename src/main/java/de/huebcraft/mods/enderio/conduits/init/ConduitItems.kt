package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

data object ConduitItems : Registrar<Item>(Registries.ITEM) {
    val CONDUITS = mutableListOf<() -> ConduitBlockItem>()
    val CONDUIT_GROUP = FabricItemGroup.builder().displayName(Text.translatable("enderio-conduits.itemgroup")).entries { displayContext, entries ->
        entries.addAll(items.map { (it.invoke() as Item).defaultStack })
    }.build()

    val ENERGY = registerConduitItem(EnderConduitTypes.ENERGY, "energy")
    val REDSTONE = registerConduitItem(EnderConduitTypes.REDSTONE, "redstone")


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