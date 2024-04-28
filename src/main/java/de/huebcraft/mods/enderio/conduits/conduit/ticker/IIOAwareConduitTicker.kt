package de.huebcraft.mods.enderio.conduits.conduit.ticker

import com.google.common.collect.ArrayListMultimap
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

interface IIOAwareConduitTicker : ILoadedAwareConduitTicker {
    override fun tickGraph(
        type: IConduitType<*>,
        loadedNodes: List<InWorldNode<*>>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        val extracts = ArrayListMultimap.create<ColorControl, Connection>()
        val inserts = ArrayListMultimap.create<ColorControl, Connection>()
        for (obj in loadedNodes) {
            obj as? InWorldNode<*> ?: continue
            for (direction in Direction.entries) {
                obj.getIOState(direction)?.let {
                    if (isRedstoneMode(type, world, obj.pos, it, isRedstoneActive)) {
                        extracts.get(it.extract).add(Connection(obj.pos, direction, obj.extendedConduitData))
                    }
                    if (it.insert != null) inserts.get(it.insert)
                        .add(Connection(obj.pos, direction, obj.extendedConduitData))
                }
            }
        }
        for (color in ColorControl.entries) {
            val eList = extracts.get(color)
            val iList = inserts.get(color)
            if (eList.isEmpty() || iList.isEmpty()) continue
            tickColoredGraph(type, iList, eList, color, world, graph, isRedstoneActive)
        }
    }

    fun tickColoredGraph(
        type: IConduitType<*>,
        inserts: List<Connection>,
        extracts: List<Connection>,
        color: ColorControl,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    )

    fun isRedstoneMode(
        type: IConduitType<*>,
        world: ServerWorld,
        pos: BlockPos,
        state: InWorldNode.IOState,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ): Boolean {
        if (!type.menuData.showRedstoneExtract) return true
        if (state.redstoneControl === RedstoneControl.ALWAYS_ACTIVE) return true
        if (state.redstoneControl === RedstoneControl.NEVER_ACTIVE) return false
        var hasRedstone = false
        for (direction in Direction.entries) {
            if (world.getEmittedRedstonePower(pos.offset(direction), direction) > 0) {
                hasRedstone = true
                break
            }
        }
        return state.redstoneControl.isActive(hasRedstone || isRedstoneActive(world, pos, state.redstoneChannel))
    }

    data class Connection(val pos: BlockPos, val direction: Direction, val data: IExtendedConduitData<*>) {
        fun move(): BlockPos = pos.offset(direction)
    }
}