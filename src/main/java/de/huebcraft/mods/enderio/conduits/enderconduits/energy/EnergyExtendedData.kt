package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.nbt.NbtCompound
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class EnergyExtendedData : IExtendedConduitData<EnergyExtendedData> {
    val lookup: EnergyStorage = ConduitEnergyStorage(this)

    var capacity = 500L
    var stored = 0L

    override fun writeNbt(): NbtCompound {
        // TODO
        return NbtCompound()
    }

    override fun readNbt(nbt: NbtCompound) {
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