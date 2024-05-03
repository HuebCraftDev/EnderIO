package de.huebcraft.mods.enderio.conduits.enderconduits.item

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

class ItemExtendedData : IExtendedConduitData<ItemExtendedData> {
    private val itemSidedData = mutableMapOf<Direction, ItemSidedData>()
    override fun readNbt(nbt: NbtCompound) {
        for (direction in Direction.entries) {
            if (nbt.contains(direction.name))
                itemSidedData[direction] = ItemSidedData.fromNbt(nbt.getCompound(direction.name))
        }
    }

    override fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        for (direction in Direction.entries) {
            val data = itemSidedData[direction] ?: continue
            nbt.put(direction.name, data.toNbt())
        }
        return nbt
    }

    override fun serializeGuiNbt(): NbtCompound {
        val nbt = NbtCompound()
        for (direction in Direction.entries) {
            val data = itemSidedData[direction] ?: continue
            nbt.put(direction.name, data.toGuiNbt())
        }
        return nbt
    }

    fun get(direction: Direction): ItemSidedData = itemSidedData.getOrDefault(direction, ItemSidedData())

    fun compute(direction: Direction): ItemSidedData = itemSidedData.computeIfAbsent(direction) { ItemSidedData() }

    class ItemSidedData {
        var roundRobin = false
        var rotatingIndex = 0
        var selfFeed = false
        var priority = 0
        companion object {
            private const val KEY_ROTATING_INDEX = "RotatingIndex"
            private const val KEY_ROUND_ROBIN = "RoundRobin"
            private const val KEY_SELF_FEED = "SelfFeed"
            private const val KEY_PRIORITY = "Priority"

            internal fun fromNbt(nbt: NbtCompound): ItemSidedData {
                val sidedData = ItemSidedData()
                sidedData.roundRobin = nbt.getBoolean(KEY_ROUND_ROBIN)
                sidedData.selfFeed = nbt.getBoolean(KEY_SELF_FEED)
                sidedData.priority = nbt.getInt(KEY_PRIORITY)
                if (nbt.contains(KEY_ROTATING_INDEX)) {
                    sidedData.rotatingIndex = nbt.getInt(KEY_ROTATING_INDEX)
                }

                return sidedData
            }
        }

        internal fun toNbt(): NbtCompound {
            val nbt = toGuiNbt()
            nbt.putInt(KEY_ROTATING_INDEX, rotatingIndex)
            return nbt
        }

        internal fun toGuiNbt(): NbtCompound {
            val nbt = NbtCompound()
            nbt.putBoolean(KEY_ROUND_ROBIN, roundRobin)
            nbt.putBoolean(KEY_SELF_FEED, selfFeed)
            nbt.putInt(KEY_PRIORITY, priority)
            return nbt
        }
    }
}