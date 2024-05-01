package de.huebcraft.mods.enderio.conduits.screen

import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import net.minecraft.util.math.Direction

data class SlotData(val direction: Direction, val conduitIndex: Int, val slotType: SlotType) {
    companion object {
        fun of(slot: Int): SlotData = SlotData(Direction.entries[slot / 3 / 9], slot / 3 % 9, SlotType.entries[slot % 3])
    }

    fun slotIndex(): Int = direction.ordinal * 3 * 9 + conduitIndex * 3 + slotType.ordinal
}
