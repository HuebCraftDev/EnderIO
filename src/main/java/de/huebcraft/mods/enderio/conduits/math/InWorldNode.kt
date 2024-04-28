package de.huebcraft.mods.enderio.conduits.math

import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.GraphObject
import dev.gigaherz.graph3.Mergeable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.EnumMap

class InWorldNode<T: IExtendedConduitData<T>>(val pos: BlockPos, val extendedConduitData: T) : GraphObject<Mergeable.Dummy> {
    companion object {
        fun <T : IExtendedConduitData<T>> createUnsafe(pos: BlockPos, data: IExtendedConduitData<*>): InWorldNode<T> = InWorldNode(pos, data as T)
    }

    private var graph: Graph<Mergeable.Dummy>? = null

    private val ioStates = EnumMap<Direction, IOState>(Direction::class.java)

    override fun getGraph(): Graph<Mergeable.Dummy>? = graph

    override fun setGraph(g: Graph<Mergeable.Dummy>?) {
        graph = g
    }

    fun pushState(direction: Direction, insert: ColorControl?, extract: ColorControl?, redstoneControl: RedstoneControl, redstoneChannel: ColorControl) {
        ioStates[direction] = IOState(insert, extract, redstoneControl, redstoneChannel)
    }

    fun getIOState(direction: Direction): IOState? = ioStates[direction]

    fun clearState(direction: Direction) = ioStates.remove(direction)

    data class IOState(val insert: ColorControl?, val extract: ColorControl?, val redstoneControl: RedstoneControl, val redstoneChannel: ColorControl) {
        fun isExtract(): Boolean = extract != null

        fun isInsert(): Boolean = insert != null
    }
}