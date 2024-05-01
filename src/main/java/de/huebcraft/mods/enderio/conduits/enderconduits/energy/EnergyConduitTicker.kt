package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.conduits.conduit.ticker.LookupAwareConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTags
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage

class EnergyConduitTicker : LookupAwareConduitTicker<EnergyStorage>() {
    override fun tickGraph(
        type: IConduitType<*>,
        loadedNodes: List<InWorldNode<*>>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        super.tickGraph(type, loadedNodes, world, graph, isRedstoneActive)
        for (node in loadedNodes) {
            val extendedData = node.extendedConduitData as EnergyExtendedData
            val energy = extendedData.lookup
            if (energy.amount == 0L) {
            }
        }
    }

    override fun tickLookupGraph(
        type: IConduitType<*>,
        inserts: List<LookupConnection>,
        extracts: List<LookupConnection>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        // TODO
    }

    override fun getLookup(): BlockApiLookup<EnergyStorage, Direction?> = EnergyStorage.SIDED

    override fun getTickRate(): Int = 1

    override fun canConnectTo(world: World, pos: BlockPos, direction: Direction): Boolean {
        return super.canConnectTo(world, pos, direction) && !world.getBlockState(pos.offset(direction))
            .isIn(ConduitTags.Blocks.ENERGY_CABLE)
    }
}