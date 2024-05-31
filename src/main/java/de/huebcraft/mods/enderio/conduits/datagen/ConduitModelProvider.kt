package de.huebcraft.mods.enderio.conduits.datagen

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.init.ConduitBlocks
import de.huebcraft.mods.enderio.conduits.init.ConduitItems
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.*
import net.minecraft.util.Identifier
import java.util.*

class ConduitModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {

    override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleState(ConduitBlocks.CONDUIT())
    }

    companion object {
        val ZERO_KEY = TextureKey.of("0")

        val CONDUIT_MODEL = Model(Optional.of(Identifier(BuildConstants.modId, "item/conduit")), Optional.empty(), ZERO_KEY)

        fun generateConduitModel(item: ConduitBlockItem, itemModelGenerator: ItemModelGenerator) {
            CONDUIT_MODEL.upload(
                ModelIds.getItemModelId(item),
                TextureMap().put(ZERO_KEY, item.typeSupplier().itemTexture),
                itemModelGenerator.writer
            )
        }
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
        for (conduitSupplier in ConduitItems.CONDUITS) {
            generateConduitModel(conduitSupplier(), itemModelGenerator)
        }
        itemModelGenerator.register(ConduitItems.CREATIVE_ENDERFACE_CONDUIT(), Models.GENERATED)
    }
}