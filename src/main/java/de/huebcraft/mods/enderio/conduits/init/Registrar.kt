package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

sealed class Registrar<T : Any>(private val registry: Registry<T>) {
    protected open val items = mutableListOf<RegisterData<*>>()

    protected open inner class RegisterData<IT : T>(private val id: String, private val itemSupplier: () -> IT) :
            () -> IT {
        private lateinit var registeredItem: IT
        override operator fun invoke() = registeredItem

        open fun register() {
            registeredItem = Registry.register(registry, Identifier(BuildConstants.modId, id), itemSupplier())
        }
    }

    protected open fun <IT : T> register(id: String, itemSupplier: () -> IT): () -> IT =
        RegisterData(id, itemSupplier).also {
            items.add(it)
        }

    internal open fun register() {
        items.forEach { it.register() }
    }
}

sealed class BlockRegistrar(registry: Registry<Block>) : Registrar<Block>(registry) {
    private inner class BlockRegisterData<IT : Block>(
        private val id: String,
        private val withItem: Boolean,
        itemSupplier: () -> IT,
    ) : RegisterData<IT>(id, itemSupplier) {
        override fun register() {
            super.register()
            if (withItem) {
                Registry.register(
                    Registries.ITEM,
                    Identifier(BuildConstants.modId, id),
                    BlockItem(invoke(), FabricItemSettings())
                )
            }
        }
    }

    override fun <IT : Block> register(id: String, itemSupplier: () -> IT) =
        register(id = id, withItem = true, itemSupplier = itemSupplier)

    protected fun <IT : Block> register(
        id: String,
        withItem: Boolean = true,
        itemSupplier: () -> IT,
    ): () -> IT = BlockRegisterData(id, withItem, itemSupplier).also(items::add)
}
