package de.huebcraft.mods.enderio.conduits.enderconduits.redstone

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList

class RedstoneExtendedData : IExtendedConduitData<RedstoneExtendedData> {
    var isActive = false
        private set
    private val activeColors = mutableListOf<ColorControl>()

    companion object {
        const val KEY_ACTIVE = "Active"
        const val KEY_COLORED_ACTIVE = "ColoredActive"
    }

    override fun readNbt(nbt: NbtCompound) {
        isActive = nbt.getBoolean(KEY_ACTIVE)
        activeColors.clear()
        if (nbt.contains(KEY_COLORED_ACTIVE, NbtList.LIST_TYPE.toInt())) {
            val colors = nbt.getList(KEY_COLORED_ACTIVE, NbtElement.INT_TYPE.toInt())
            for (colorNbt in colors) {
                colorNbt as? NbtInt ?: continue
                val index = colorNbt.intValue()
                if (index < 0 || index >= ColorControl.entries.size) continue
                activeColors.add(ColorControl.entries[index])
            }
        }
    }

    override fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putBoolean(KEY_ACTIVE, isActive)
        val colors = NbtList()
        for (color in activeColors) {
            colors.add(NbtInt.of(color.ordinal))
        }
        nbt.put(KEY_COLORED_ACTIVE, colors)
        return nbt
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