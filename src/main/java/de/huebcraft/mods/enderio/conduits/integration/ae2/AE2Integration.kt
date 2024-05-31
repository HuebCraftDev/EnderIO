package de.huebcraft.mods.enderio.conduits.integration.ae2

import appeng.api.networking.IInWorldGridNodeHost
import de.huebcraft.mods.enderio.api.integration.EnderIOPlugin
import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitBlocks
import de.huebcraft.mods.enderio.conduits.init.ConduitItems
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import de.huebcraft.mods.enderio.conduits.init.ModBlockEntities
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class AE2Integration : EnderIOPlugin {
    companion object {
        private val DENSE_TYPE =
            Registry.register(ConduitTypes.REGISTRY, Identifier(BuildConstants.modId, "dense_me"), AE2ConduitType(true))
        private val NORMAL_TYPE =
            Registry.register(ConduitTypes.REGISTRY, Identifier(BuildConstants.modId, "me"), AE2ConduitType(false))
        val DENSE_ITEM = createConduitItem({ DENSE_TYPE }, "dense_me")
        val NORMAL_ITEM = createConduitItem({ NORMAL_TYPE }, "me")

        private fun createConduitItem(type: () -> IConduitType<*>, itemName: String): () -> ConduitBlockItem {
            val item = Registry.register(Registries.ITEM,
                Identifier(BuildConstants.modId, itemName + "_conduit"), ConduitBlockItem(type, ConduitBlocks.CONDUIT(), FabricItemSettings())
            )
            ConduitItems.CONDUITS.add { item }
            return { item }
        }
    }

    override fun onEnderIOInitialized() {
        IInWorldGridNodeHost.LOOKUP.registerForBlockEntity(
            ConduitBlockEntity.createConduitLookup(IInWorldGridNodeHost.LOOKUP), ModBlockEntities.CONDUIT_BLOCK_ENTITY()
        )
    }

    override fun onEnglishTranslationsAdded(translationBuilder: FabricLanguageProvider.TranslationBuilder) {
        translationBuilder.add(DENSE_ITEM(), "ME Dense Conduit")
        translationBuilder.add(NORMAL_ITEM(), "ME Conduit")
    }
}