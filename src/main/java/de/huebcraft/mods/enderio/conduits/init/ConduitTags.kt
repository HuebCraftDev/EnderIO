package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object ConduitTags {
    object Blocks {
        // TODO Provider
        val ENERGY_CABLE = TagKey.of(RegistryKeys.BLOCK, Identifier(BuildConstants.modId, "energy_cable"))
        val REDSTONE_CONNECTABLE = TagKey.of(RegistryKeys.BLOCK, Identifier(BuildConstants.modId, "redstone_connectable"))
    }
}