package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.registry.Registries
import net.minecraft.text.Text

object ModItems : de.huebcraft.mods.enderio.conduits.init.Registrar<Item>(Registries.ITEM) {
    val groupContents = mutableListOf<Item>()
    val CONDUIT_GROUP = FabricItemGroup.builder().displayName(Text.translatable("enderio-conduits.itemgroup")).entries { displayContext, entries ->
        entries.addAll(items.map { it. })
    }.build()

    val ENERGY = registerConduitItem(ModConduitTypes.ENERGY, "energy", FabricItemSettings())

    private fun registerConduitItem(type: () -> IConduitType<*>, name: String, settings: Settings): () -> ConduitBlockItem =
        register(
            name + "_conduit"
        ) { ConduitBlockItem(type, ModBlocks.CONDUIT(), settings) }

    override fun <IT : Item> register(id: String, itemSupplier: () -> IT): () -> IT {
        return super.register(id, itemSupplier)
    }
}