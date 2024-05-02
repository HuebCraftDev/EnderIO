package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.conduit.type.SimpleConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTags
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.joml.Vector2i
import team.reborn.energy.api.EnergyStorage

class EnergyConduitType : SimpleConduitType<EnergyExtendedData>(
    Identifier(BuildConstants.modId, "block/conduit/energy"),
    EnergyConduitTicker(),
    ::EnergyExtendedData,
    IClientConduitData.Simple(EnderConduitTypes.ICON_TEXTURE, Vector2i(0, 24)),
    IConduitMenuData.ENERGY
) {
    override fun getDefaultConnection(
        world: World, pos: BlockPos, direction: Direction
    ): IConduitType.ConduitConnectionData {
        val lookup = EnergyStorage.SIDED.find(world, pos.offset(direction), direction.opposite)
            ?: return super.getDefaultConnection(world, pos, direction)
        return IConduitType.ConduitConnectionData(
            lookup.supportsInsertion(), lookup.supportsExtraction(), RedstoneControl.ALWAYS_ACTIVE
        )
    }

    override fun <A, C> proxyLookup(
        lookup: BlockApiLookup<A, C>,
        extendedConduitData: EnergyExtendedData,
        world: World,
        pos: BlockPos,
        direction: Direction?,
        state: InWorldNode.IOState?
    ): A? {
        if (EnergyStorage.SIDED === lookup && state?.isExtract() != false && (direction == null || world.getBlockState(
                pos.offset(direction)
            ).isIn(ConduitTags.Blocks.ENERGY_CABLE))
        ) {
            @Suppress("UNCHECKED_CAST") return extendedConduitData.lookup as? A
        }
        return null
    }
}