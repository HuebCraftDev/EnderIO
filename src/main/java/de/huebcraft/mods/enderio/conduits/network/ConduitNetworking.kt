package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object ConduitNetworking {
    val DATA_SLOT = Identifier(BuildConstants.modId, "s2c_data_slot_update")

    private fun handleConnectionState(
        packet: C2SSetConduitConnectionState,
        player: ServerPlayerEntity,
        responseSender: PacketSender
    ) {
        val be = player.world.getBlockEntity(packet.pos) as? ConduitBlockEntity ?: return
        be.handleConnectionStateUpdate(packet.direction, packet.type, packet.state)
    }

    private fun handleExtendedData(
        packet: C2SSetConduitExtendedData,
        player: ServerPlayerEntity,
        responseSender: PacketSender
    ) {
        val be = player.world.getBlockEntity(packet.pos) as? ConduitBlockEntity ?: return
        be.handleExtendedDataUpdate(packet.type, packet.extendedData)
    }

    fun registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(C2SSetConduitConnectionState.TYPE, ::handleConnectionState)
        ServerPlayNetworking.registerGlobalReceiver(C2SSetConduitExtendedData.TYPE, ::handleExtendedData)
    }

    private fun handleDataSlot(
        client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender
    ) {
        val be = client.world?.getBlockEntity(buf.readBlockPos()) as? ConduitBlockEntity ?: return
        be.clientHandleBufferSync(PacketByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())))
    }

    fun registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(DATA_SLOT, ::handleDataSlot)
    }
}