package de.huebcraft.mods.enderio.conduits.conduit.ticker

import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

interface IConduitTicker {
    fun tickGraph(
        type: IConduitType<*>,
        graph: Graph<Mergeable.Dummy>,
        world: ServerWorld,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    )

    fun getTickRate(): Int = 5

    fun canConnectTo(world: World, pos: BlockPos, direction: Direction): Boolean

    fun hasConnectionDelay(): Boolean = false

    fun canConnectTo(thisType: IConduitType<*>, other: IConduitType<*>): Boolean {
        return thisType === other
    }
}