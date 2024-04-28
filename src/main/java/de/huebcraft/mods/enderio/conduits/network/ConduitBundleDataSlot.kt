package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.conduits.conduit.ConduitBundle
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class ConduitBundleDataSlot(getter: () -> ConduitBundle) : NetworkDataSlot<ConduitBundle>(getter, {}) {

    override fun readNbt(nbt: NbtCompound) {
        getter().deserializeNbt(nbt)
    }

    override fun fromNbt(nbt: NbtCompound): ConduitBundle = throw IllegalStateException()

    override fun fromBuffer(buf: PacketByteBuf): ConduitBundle {
        val value = getter()
        value.deserializeNbt(buf.readNbt()!!)
        return value
    }

    override fun toBuffer(buf: PacketByteBuf, value: ConduitBundle) {
        buf.writeNbt(value.serializeNbt())
    }

    override fun hashCode(value: ConduitBundle): Int = value.dataVersion

    override fun toNbt(value: ConduitBundle): NbtCompound = value.serializeNbt()
}