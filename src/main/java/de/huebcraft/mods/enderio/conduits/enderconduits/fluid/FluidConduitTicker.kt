package de.huebcraft.mods.enderio.conduits.enderconduits.fluid

import de.huebcraft.mods.enderio.conduits.conduit.ticker.LookupAwareConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

@Suppress("UnstableApiUsage")
class FluidConduitTicker(private val lockFluids: Boolean, private val fluidRate: Int) :
    LookupAwareConduitTicker<Storage<FluidVariant>>() {

    override fun tickGraph(
        type: IConduitType<*>,
        loadedNodes: List<InWorldNode<*>>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        var shouldReset = false
        for (loadedNode in loadedNodes) {
            val extendedData = loadedNode.extendedConduitData as FluidExtendedData
            if (extendedData.shouldReset) {
                shouldReset = true
                extendedData.shouldReset = false
            }
        }
        if (shouldReset) {
            for (loadedNode in loadedNodes) {
                (loadedNode.extendedConduitData as FluidExtendedData).lockedFluid = null
            }
        }
        super.tickGraph(type, loadedNodes, world, graph, isRedstoneActive)
    }

    override fun tickLookupGraph(
        type: IConduitType<*>,
        inserts: List<LookupConnection>,
        extracts: List<LookupConnection>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        for (extractLookup in extracts) {
            val extractHandler = extractLookup.lookup
            val extendedData = extractLookup.data as FluidExtendedData
            Transaction.openOuter().use {
                if (extendedData.lockedFluid == null) {
                }
                val extracted = extractHandler.extract(FluidVariant.of(extendedData.lockedFluid), fluidRate.toLong(), it)
            }
        }
    }

    override fun getLookup(): BlockApiLookup<Storage<FluidVariant>, Direction?> = FluidStorage.SIDED
}