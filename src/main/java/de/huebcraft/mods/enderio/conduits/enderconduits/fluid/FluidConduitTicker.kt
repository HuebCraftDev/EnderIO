package de.huebcraft.mods.enderio.conduits.enderconduits.fluid

import de.huebcraft.mods.enderio.conduits.conduit.ticker.LookupAwareConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class FluidConduitTicker(private val lockFluids: Boolean, private val fluidRate: Int) :
    LookupAwareConduitTicker<Storage<FluidVariant>>() {
    override fun tickLookupGraph(
        type: IConduitType<*>,
        inserts: List<LookupConnection>,
        extracts: List<LookupConnection>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getLookup(): BlockApiLookup<Storage<FluidVariant>, Direction?> {
        TODO("Not yet implemented")
    }
}