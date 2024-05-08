package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class EnergyExtendedData : IExtendedConduitData<EnergyExtendedData> {
    companion object {
        const val ENERGY_STORED: String = "EnergyStored"
        const val ENERGY_MAX_STORED: String = "MaxEnergyStored"
    }

    private val energySidedData = mutableMapOf<Direction, EnergySidedData>()
    val lookup: EnergyStorage = ConduitEnergyStorage(this)

    var capacity = 500L
    var stored = 0L

    override fun writeNbt(): NbtCompound {
        val tag = NbtCompound()
        for (direction in Direction.entries) {
            val sidedData = energySidedData[direction]
            if (sidedData != null) {
                tag.put(direction.name, sidedData.writeNbt())
            }
        }
        tag.putLong(ENERGY_MAX_STORED, capacity)
        tag.putLong(ENERGY_STORED, stored)
        return tag
    }

    override fun readNbt(nbt: NbtCompound) {
        energySidedData.clear()
        for (direction in Direction.entries) {
            if (nbt.contains(direction.name)) {
                energySidedData[direction] = EnergySidedData.readNbt(nbt.getCompound(direction.name))
            }
        }
        if (nbt.contains(ENERGY_MAX_STORED)) {
            capacity = nbt.getLong(ENERGY_MAX_STORED).coerceAtLeast(500)
        }
        if (nbt.contains(ENERGY_STORED)) {
            stored = nbt.getLong(ENERGY_STORED)
        }
    }

    private class EnergySidedData {
        var rotatingIndex = 0
        companion object {
            private val KEY_ROTATING_INDEX = "RotatingIndex"

            fun readNbt(nbt: NbtCompound): EnergySidedData {
                val energyData = EnergySidedData()
                if (nbt.contains(KEY_ROTATING_INDEX, NbtElement.INT_TYPE.toInt()))
                    energyData.rotatingIndex = nbt.getInt(KEY_ROTATING_INDEX)
                return energyData
            }
        }
        fun writeNbt(): NbtCompound {
            val nbt = NbtCompound()
            nbt.putInt(KEY_ROTATING_INDEX, rotatingIndex)
            return NbtCompound()
        }
    }

    @Suppress("UnstableApiUsage")
    private class ConduitEnergyStorage(val extendedData: EnergyExtendedData) : SnapshotParticipant<Long>(), EnergyStorage {
        override fun insert(maxAmount: Long, transaction: TransactionContext?): Long {
            StoragePreconditions.notNegative(maxAmount)
            val inserted = min(Double.MAX_VALUE, min(maxAmount.toDouble(), (capacity - amount).toDouble())).toLong()

            if (inserted > 0) {
                updateSnapshots(transaction)
                extendedData.stored += inserted
                return inserted
            }

            return 0
        }

        override fun extract(maxAmount: Long, transaction: TransactionContext?): Long {
            StoragePreconditions.notNegative(maxAmount)
            val extracted =
                min(Double.MAX_VALUE, min(maxAmount.toDouble(), amount.toDouble())).toLong()

            if (extracted > 0) {
                updateSnapshots(transaction)
                extendedData.stored -= extracted
                return extracted
            }

            return 0
        }

        override fun getAmount(): Long = extendedData.stored

        override fun getCapacity(): Long = extendedData.capacity

        override fun createSnapshot(): Long = extendedData.stored

        override fun readSnapshot(snapshot: Long) {
            extendedData.stored = snapshot
        }
    }
}