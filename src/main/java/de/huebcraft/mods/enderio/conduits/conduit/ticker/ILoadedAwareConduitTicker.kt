package de.huebcraft.mods.enderio.conduits.conduit.ticker

import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

interface ILoadedAwareConduitTicker : IConduitTicker {
    override fun tickGraph(
        type: IConduitType<*>,
        graph: Graph<Mergeable.Dummy>,
        world: ServerWorld,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        val nodes = mutableListOf<InWorldNode<*>>()
        for (obj in graph.objects) {
            obj as? InWorldNode<*> ?: continue
            if (isLoaded(world, obj.pos)) nodes.add(obj)
        }
        tickGraph(type, nodes, world, graph, isRedstoneActive)
    }

    fun tickGraph(
        type: IConduitType<*>,
        loadedNodes: List<InWorldNode<*>>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    )

    fun isLoaded(world: ServerWorld, pos: BlockPos): Boolean = world.isChunkLoaded(pos) && world.shouldTick(pos)
}