package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.SimpleConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTags
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage

class EnergyConduitType : SimpleConduitType<EnergyExtendedData>(
    Identifier(BuildConstants.modId, "block/conduit/energy"), EnergyConduitTicker, ::EnergyExtendedData
) {
    override fun <K> proxyLookup(
        lookup: BlockApiLookup<K, Direction?>,
        extendedConduitData: EnergyExtendedData,
        world: World,
        pos: BlockPos,
        direction: Direction?,
        state: InWorldNode.IOState?
    ): K? {
        if (EnergyStorage.SIDED === lookup && state?.isExtract() != false && (direction == null || world.getBlockState(
                pos.offset(direction)
            ).isIn(ConduitTags.Blocks.ENERGY_CABLE))
        ) {
            @Suppress("UNCHECKED_CAST") return extendedConduitData.lookup as? K
        }
        return null
    }
}