package de.huebcraft.mods.enderio.conduits.enderconduits.energy

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.nbt.NbtCompound
import team.reborn.energy.api.EnergyStorage

class EnergyExtendedData : IExtendedConduitData<EnergyExtendedData> {
    val lookup: EnergyStorage = ConduitEnergyStorage(this)

    override fun writeNbt(): NbtCompound {
        TODO("Not yet implemented")
    }

    override fun readNbt(nbt: NbtCompound) {
        TODO("Not yet implemented")
    }

    private class ConduitEnergyStorage(val extendedData: EnergyExtendedData) : EnergyStorage {
        override fun insert(maxAmount: Long, transaction: TransactionContext?): Long {
            TODO("Not yet implemented")
        }

        override fun extract(maxAmount: Long, transaction: TransactionContext?): Long {
            TODO("Not yet implemented")
        }

        override fun getAmount(): Long {
            TODO("Not yet implemented")
        }

        override fun getCapacity(): Long {
            TODO("Not yet implemented")
        }

    }
}