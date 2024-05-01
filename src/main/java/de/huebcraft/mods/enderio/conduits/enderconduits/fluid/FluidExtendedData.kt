package de.huebcraft.mods.enderio.conduits.enderconduits.fluid

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.nbt.NbtCompound

class FluidExtendedData(private val isMultiFluid: Boolean) : IExtendedConduitData<FluidExtendedData> {
    override fun readNbt(nbt: NbtCompound) {
        TODO("Not yet implemented")
    }

    override fun writeNbt(): NbtCompound {
        TODO("Not yet implemented")
    }
}