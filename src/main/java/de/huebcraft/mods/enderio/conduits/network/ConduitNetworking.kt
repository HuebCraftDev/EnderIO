package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object ConduitNetworking {
    val CONDUIT_CONNECTION_STATE = Identifier(BuildConstants.modId, "c2s_conduit_connection_state")
    val CONDUIT_EXTENDED_DATA = Identifier(BuildConstants.modId, "c2s_conduit_extended_data")
    val DATA_SLOT = Identifier(BuildConstants.modId, "s2c_data_slot_update")

    fun sendConnectionState(
        pos: BlockPos, direction: Direction, type: IConduitType<*>, state: IConnectionState.DynamicConnectionState
    ) {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(pos)
        buf.writeEnumConstant(direction)
        buf.writeIdentifier(ConduitTypes.REGISTRY.getId(type))
        state.toNetwork(buf)
        ClientPlayNetworking.send(CONDUIT_CONNECTION_STATE, buf)
    }

    private fun handleConnectionState(
        server: MinecraftServer,
        player: ServerPlayerEntity,
        handler: ServerPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        val be = player.world.getBlockEntity(buf.readBlockPos()) as? ConduitBlockEntity ?: return
        val direction = buf.readEnumConstant(Direction::class.java)
        val type = ConduitTypes.REGISTRY.get(buf.readIdentifier()) ?: return
        val state = IConnectionState.DynamicConnectionState.fromNetwork(buf)
        be.handleConnectionStateUpdate(direction, type, state)
    }

    fun sendExtendedData(pos: BlockPos, type: IConduitType<*>, extendedData: NbtCompound) {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(pos)
        buf.writeIdentifier(ConduitTypes.REGISTRY.getId(type))
        buf.writeNbt(extendedData)
        ClientPlayNetworking.send(CONDUIT_EXTENDED_DATA, buf)
    }

    private fun handleExtendedData(
        server: MinecraftServer,
        player: ServerPlayerEntity,
        handler: ServerPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        val be = player.world.getBlockEntity(buf.readBlockPos()) as? ConduitBlockEntity ?: return
        val type = ConduitTypes.REGISTRY.get(buf.readIdentifier()) ?: return
        val nbt = buf.readNbt() ?: return
        be.handleExtendedDataUpdate(type, nbt)
    }

    private fun handleDataSlot(
        client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender
    ) {
        val be = client.world?.getBlockEntity(buf.readBlockPos()) as? ConduitBlockEntity ?: return
        be.clientHandleBufferSync(PacketByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())))
    }

    fun registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CONDUIT_CONNECTION_STATE, ::handleConnectionState)
        ServerPlayNetworking.registerGlobalReceiver(CONDUIT_EXTENDED_DATA, ::handleExtendedData)
    }

    fun registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(DATA_SLOT, ::handleDataSlot)
    }
}