package de.huebcraft.mods.enderio.conduits.network

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

abstract class NetworkDataSlot<T>(val getter: () -> T, val setter: (T) -> Unit) {
    private var cachedHash = 0
    fun writeNbt(fullUpdate: Boolean): NbtCompound? {
        val value = getter()
        val hash = hashCode(value)
        if (!fullUpdate && cachedHash == hash) {
            return null
        }
        cachedHash = hash
        return toNbt(value)
    }

    open fun readNbt(nbt: NbtCompound) {
        setter(fromNbt(nbt))
    }

    fun writeBuffer(buf: PacketByteBuf) {
        val value = getter()
        cachedHash = hashCode(value)
        toBuffer(buf, value)
    }

    open fun readBuffer(buf: PacketByteBuf) {
        setter(fromBuffer(buf))
    }

    abstract fun fromNbt(nbt: NbtCompound): T

    abstract fun toNbt(value: T): NbtCompound

    abstract fun fromBuffer(buf: PacketByteBuf): T

    abstract fun toBuffer(buf: PacketByteBuf, value: T)

    fun needsUpdate(): Boolean {
        val value = getter()
        return hashCode(value) != cachedHash
    }

    protected open fun hashCode(value: T) = value.hashCode()
}