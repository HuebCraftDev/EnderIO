package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

@Environment(EnvType.CLIENT)
data class ConduitRenderData(
    val connectionData: Map<Direction, ConnectionData>,
    val extendedData: Map<IConduitType<*>, IExtendedConduitData<*>>,
    val facades: Map<Direction, BlockState>?
) {

    data class ConnectionData(val connectionStates: List<Pair<IConduitType<*>, IConnectionState>>)
}