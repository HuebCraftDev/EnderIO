package de.huebcraft.mods.enderio.conduits.enderconduits.redstone

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import net.minecraft.nbt.NbtCompound

class RedstoneExtendedData : IExtendedConduitData<RedstoneExtendedData> {
    var isActive = false
        private set
    private val activeColors = mutableListOf<ColorControl>()

    override fun readNbt(nbt: NbtCompound) {
        TODO("Not yet implemented")
    }

    override fun writeNbt(): NbtCompound {
        TODO("Not yet implemented")
    }

    fun isActive(control: ColorControl): Boolean = activeColors.contains(control)

    fun clearActive() {
        activeColors.clear()
        isActive = false
    }

    fun setActiveColor(control: ColorControl) {
        if (activeColors.contains(control)) return
        isActive = true
        activeColors.add(control)
    }

    override fun deepCopy(): RedstoneExtendedData {
        val data = RedstoneExtendedData()
        data.isActive = isActive
        return data
    }
}