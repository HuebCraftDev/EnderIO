package de.huebcraft.mods.enderio.conduits.conduit.ticker

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class LookupAwareConduitTicker<T> : IIOAwareConduitTicker {
    final override fun tickColoredGraph(
        type: IConduitType<*>,
        inserts: List<IIOAwareConduitTicker.Connection>,
        extracts: List<IIOAwareConduitTicker.Connection>,
        color: ColorControl,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        val insertLookup = mutableListOf<LookupConnection>()
        for (insert in inserts) {
            val lookup = getLookup().find(world, insert.move(), insert.direction.opposite)
            if (lookup != null) {
                insertLookup.add(
                    LookupConnection(
                        lookup,
                        insert.data,
                        insert.direction
                    )
                )
            }
        }
        if (insertLookup.isNotEmpty()) {
            val extractLookup = mutableListOf<LookupConnection>()
            for (extract in extracts) {
                val lookup = getLookup().find(world, extract.move(), extract.direction.opposite)
                if (lookup != null) {
                    extractLookup.add(
                        LookupConnection(
                            lookup,
                            extract.data,
                            extract.direction
                        )
                    )
                }
            }
            if (extractLookup.isNotEmpty()) {
                tickLookupGraph(type, insertLookup, extractLookup, world, graph, isRedstoneActive)
            }
        }
    }

    override fun canConnectTo(world: World, pos: BlockPos, direction: Direction): Boolean =
        getLookup().find(world, pos.offset(direction), direction.opposite) != null

    protected abstract fun tickLookupGraph(
        type: IConduitType<*>,
        inserts: List<LookupConnection>,
        extracts: List<LookupConnection>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    )

    protected abstract fun getLookup(): BlockApiLookup<T, Direction?>

    inner class LookupConnection(val lookup: T, val data: IExtendedConduitData<*>, val direction: Direction)
}