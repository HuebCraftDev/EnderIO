package de.huebcraft.mods.enderio.conduits.enderconduits.redstone

import de.huebcraft.mods.enderio.conduits.conduit.ticker.IIOAwareConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTags
import de.huebcraft.mods.enderio.conduits.init.ModBlocks
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class RedstoneConduitTicker : IIOAwareConduitTicker {

    private val activeColors = arrayListOf<ColorControl>()

    override fun canConnectTo(world: World, pos: BlockPos, direction: Direction): Boolean {
        val neighbor = pos.offset(direction)
        val state = world.getBlockState(neighbor)
        return state.isIn(ConduitTags.Blocks.REDSTONE_CONNECTABLE) || state.emitsRedstonePower()
    }

    override fun tickGraph(
        type: IConduitType<*>,
        graph: Graph<Mergeable.Dummy>,
        world: ServerWorld,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        val nodes = arrayListOf<InWorldNode<*>>()
        for (obj in graph.objects) {
            obj as? InWorldNode<*> ?: continue
            nodes.add(obj)
        }
        activeColors.clear()
        tickGraph(type, nodes.filter { isLoaded(world, it.pos) }, world, graph, isRedstoneActive)
        for (node in nodes) {
            val data = node.extendedConduitData as RedstoneExtendedData
            data.clearActive()
            for (activeColor in activeColors) {
                data.setActiveColor(activeColor)
            }
        }
    }

    override fun tickColoredGraph(
        type: IConduitType<*>,
        inserts: List<IIOAwareConduitTicker.Connection>,
        extracts: List<IIOAwareConduitTicker.Connection>,
        color: ColorControl,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        for (extract in extracts) {
            if (world.isEmittingRedstonePower(extract.move(), extract.direction)) {
                activeColors.add(color)
                break
            }
        }
        for (insert in inserts) {
            world.updateNeighbor(insert.move(), ModBlocks.CONDUIT(), insert.pos)
        }
    }

    override fun getTickRate(): Int = 2
}