package de.huebcraft.mods.enderio.conduits.enderconduits.fluid

import de.huebcraft.mods.enderio.conduits.Main
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class FluidExtendedData(val isMultiFluid: Boolean) : IExtendedConduitData<FluidExtendedData> {
    var lockedFluid: Fluid? = null
    var shouldReset = false

    override fun canConnectTo(other: FluidExtendedData): Boolean =
        lockedFluid == null || other.lockedFluid == null || lockedFluid === other.lockedFluid

    override fun onConnectTo(other: FluidExtendedData) {
        if (lockedFluid != null) {
            if (other.lockedFluid != null && lockedFluid !== other.lockedFluid) {
                Main.LOGGER.warn("incompatible fluid conduits merged")
            }
            other.lockedFluid = lockedFluid
        } else if (other.lockedFluid != null) {
            lockedFluid = other.lockedFluid
        }
    }

    companion object {
        private const val SHOULD_RESET = "ShouldReset"
    }

    override fun readNbt(nbt: NbtCompound) {
        if (nbt.contains("Fluid") && !isMultiFluid) {
            val fluid = nbt.getString("Fluid")
            lockedFluid = if (fluid == "null" || Registries.FLUID.get(Identifier(fluid)) === Fluids.EMPTY) {
                null
            } else {
                Registries.FLUID.get(Identifier(fluid))
            }
        } else {
            lockedFluid = null
        }
        if (nbt.contains(SHOULD_RESET)) {
            shouldReset = nbt.getBoolean(SHOULD_RESET)
        }
    }

    override fun serializeRenderNbt(): NbtCompound {
        return writeNbt()
    }

    override fun serializeGuiNbt(): NbtCompound {
        val nbt = writeNbt()
        nbt.putBoolean(SHOULD_RESET, shouldReset)
        return nbt
    }

    override fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        if (!isMultiFluid) {
            if (lockedFluid != null) {
                nbt.putString("Fluid", Registries.FLUID.getKey(lockedFluid).toString())
            } else {
                nbt.putString("Fluid", "null")
            }
        }
        return nbt
    }
}