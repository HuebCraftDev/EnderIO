package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

data class C2SSetConduitConnectionState(
    val pos: BlockPos,
    val direction: Direction,
    val type: IConduitType<*>,
    val state: IConnectionState.DynamicConnectionState
) : FabricPacket {
    companion object {
        val TYPE: PacketType<C2SSetConduitConnectionState> =
            PacketType.create(Identifier(BuildConstants.modId, "c2s_conduit_connection_state"), ::C2SSetConduitConnectionState)
    }

    constructor(buf: PacketByteBuf) : this(
        buf.readBlockPos(),
        buf.readEnumConstant(Direction::class.java),
        ConduitTypes.REGISTRY.get(buf.readIdentifier())!!,
        IConnectionState.DynamicConnectionState.fromNetwork(buf)
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeEnumConstant(direction)
        buf.writeIdentifier(ConduitTypes.REGISTRY.getId(type))
        state.toNetwork(buf)
    }

    override fun getType(): PacketType<*> = TYPE
}