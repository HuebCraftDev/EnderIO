package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

data class C2SSetConduitExtendedData(val pos: BlockPos, val type: IConduitType<*>, val extendedData: NbtCompound) : FabricPacket {
    companion object {
        val TYPE: PacketType<C2SSetConduitExtendedData> =
            PacketType.create(Identifier(BuildConstants.modId, "c2s_conduit_extended_data"), ::C2SSetConduitExtendedData)
    }

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos(), ConduitTypes.REGISTRY.get(buf.readIdentifier())!!, buf.readNbt()!!)

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeIdentifier(ConduitTypes.REGISTRY.getId(type))
        buf.writeNbt(extendedData)
    }

    override fun getType(): PacketType<*> = TYPE
}