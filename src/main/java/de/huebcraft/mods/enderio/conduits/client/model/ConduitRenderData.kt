package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

@Environment(EnvType.CLIENT)
data class ConduitRenderData(val connectionData: Map<Direction, ConnectionData>, val extendedData: Map<IConduitType<*>, IExtendedConduitData<*>>, val facades: Map<Direction, BlockState>?) {

    data class ConnectionData(val connectionStates: Array<Pair<IConduitType<*>, ConnectionStateData>>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ConnectionData) return false

            if (!connectionStates.contentEquals(other.connectionStates)) return false

            return true
        }

        override fun hashCode(): Int {
            return connectionStates.contentHashCode()
        }
    }

    data class ConnectionStateData(
        val staticConnection: IConnectionState.StaticConnectionStates?,
        val insert: ColorControl?,
        val extract: ColorControl?,
        val control: RedstoneControl?,
        val redstoneChannel: ColorControl?
    )

    fun findMainAxis() {

    }

    // get connections with types
    // get extended conduit data of connected nodes
    // get connection state
    // get facade
}