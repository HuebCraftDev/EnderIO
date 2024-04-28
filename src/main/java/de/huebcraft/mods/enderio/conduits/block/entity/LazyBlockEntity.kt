package de.huebcraft.mods.enderio.conduits.block.entity

import de.huebcraft.mods.enderio.conduits.network.NetworkDataSlot
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class LazyBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BlockEntity(type, pos, state) {
    private val dataSlots = mutableListOf<NetworkDataSlot<*>>()
    private val afterDataSync = mutableListOf<() -> Unit>()

    companion object {
        const val INDEX = "Index"
        const val DATA = "Data"
    }

    fun serverTick(world: World, pos: BlockPos) {
        if (!world.isClient) {
            sync()
            world.markDirty(pos)
        }
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val dataList = NbtList()
        for (i in dataSlots.indices) {
            val slot = dataSlots[i]
            val nbt = slot.writeNbt(true) ?: continue
            val slotNbt = NbtCompound()
            slotNbt.putInt(INDEX, i)
            slotNbt.put(DATA, nbt)
            dataList.add(slotNbt)
        }

        val data = NbtCompound()
        data.put(DATA, dataList)
        return data
    }

    override fun readNbt(nbt: NbtCompound) {
        if (nbt.contains(DATA, NbtElement.LIST_TYPE.toInt())) {
            val dataList = nbt.getList(DATA, NbtElement.COMPOUND_TYPE.toInt())
            for (entry in dataList) {
                if (entry is NbtCompound) {
                    dataSlots[entry.getInt(INDEX)].fromNbt(entry.getCompound(DATA))
                }
            }
            for (task in afterDataSync) task()
        }
    }

    private fun createBufferSlotUpdate(): PacketByteBuf? {
        val buf = PacketByteBufs.create()
        var amount = 0
        for (i in dataSlots.indices) {
            val dataSlot = dataSlots[i]
            if (dataSlot.needsUpdate()) {
                ++amount
                buf.writeInt(i)
                dataSlot.writeBuffer(buf)
            }
        }
        if (amount == 0) return null
        val res = PacketByteBufs.create()
        res.writeInt(amount)
        res.writeBytes(buf)
        return res
    }

    fun <T : NetworkDataSlot<*>> addDataSlot(slot: T): T {
        dataSlots.add(slot)
        return slot
    }

    fun addAfterSyncRunnable(task: () -> Unit) {
        afterDataSync.add(task)
    }

    @Environment(EnvType.SERVER)
    fun sync() {
        val syncData = createBufferSlotUpdate() ?: return
        TODO("Send to all tracking")
    }

    @Environment(EnvType.CLIENT)
    fun clientHandleBufferSync(buf: PacketByteBuf) {
        for (i in 0 until buf.readInt()) {
            dataSlots[buf.readInt()].fromBuffer(buf)
        }

        for (task in afterDataSync) task()
    }

    @Environment(EnvType.SERVER)
    fun serverHandleBufferChange(buf: PacketByteBuf) {
        val index = buf.readInt()
        dataSlots[index].fromBuffer(buf)
    }
}