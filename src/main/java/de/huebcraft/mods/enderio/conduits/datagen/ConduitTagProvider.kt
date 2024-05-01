package de.huebcraft.mods.enderio.conduits.datagen

import de.huebcraft.mods.enderio.conduits.init.ConduitBlocks
import de.huebcraft.mods.enderio.conduits.init.ConduitTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class ConduitTagProvider(
    output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : BlockTagProvider(output, registriesFuture) {
    override fun configure(arg: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(ConduitTags.Blocks.REDSTONE_CONNECTABLE).add(
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.REDSTONE_LAMP,
            Blocks.NOTE_BLOCK,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.POWERED_RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.MOVING_PISTON
        ).forceAddTag(BlockTags.DOORS).forceAddTag(BlockTags.TRAPDOORS).forceAddTag(BlockTags.REDSTONE_ORES).addOptional(
            Identifier("c", "redstone_dusts")
        )
        getOrCreateTagBuilder(ConventionalBlockTags.MOVEMENT_RESTRICTED).add(ConduitBlocks.CONDUIT())
    }
}