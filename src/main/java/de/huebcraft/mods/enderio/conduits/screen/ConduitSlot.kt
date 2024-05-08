package de.huebcraft.mods.enderio.conduits.screen

import de.huebcraft.mods.enderio.conduits.conduit.ConduitBundle
import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import de.huebcraft.mods.enderio.conduits.mixin.SlotAccessor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class ConduitSlot(
    private val bundle: ConduitBundle,
    handler: ItemHandler,
    private val visibleDirection: () -> Direction,
    private val visibleForDirection: Direction,
    private val visibleType: () -> Int,
    private val visibleForType: Int,
    private val slotType: SlotType
) : HandledSlot(handler, SlotData(visibleForDirection, visibleForType, slotType).slotIndex(), slotType.x, slotType.y) {
    override fun canInsert(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        return isVisible() && super.canInsert(stack)
    }

    override fun canTakeItems(playerEntity: PlayerEntity?): Boolean {
        return isVisible() && super.canTakeItems(playerEntity)
    }

    override fun takeStack(amount: Int): ItemStack {
        return if (isVisible()) super.takeStack(amount) else ItemStack.EMPTY
    }

    fun updateVisibilityPosition() {
        if (isVisible()) {
            (this as SlotAccessor).setX(slotType.x)
            (this as SlotAccessor).setY(slotType.y)
        } else {
            (this as SlotAccessor).setX(Int.MIN_VALUE)
            (this as SlotAccessor).setY(Int.MIN_VALUE)
        }
    }

    private fun isVisible(): Boolean =
        visibleDirection() === visibleForDirection && visibleType() == visibleForType && bundle.types.size > visibleForType && slotType.isAvailableFor(
            bundle.types[visibleForType].menuData
        )

    override fun onQuickTransfer(newItem: ItemStack?, original: ItemStack?) {}
}