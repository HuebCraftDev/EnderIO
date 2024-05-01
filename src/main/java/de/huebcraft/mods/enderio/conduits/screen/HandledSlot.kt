package de.huebcraft.mods.enderio.conduits.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

open class HandledSlot(val itemHandler: ItemHandler, index: Int, x: Int, y: Int) :
    Slot(SimpleInventory(0), index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        return itemHandler.canInsert(index, stack)
    }

    override fun getStack(): ItemStack {
        return itemHandler.getStack(index)
    }

    override fun setStackNoCallbacks(stack: ItemStack) {
        itemHandler.setStack(index, stack)
        markDirty()
    }

    override fun onQuickTransfer(newItem: ItemStack?, original: ItemStack?) {}

    override fun getMaxItemCount(): Int {
        return itemHandler.getMaxItemCount(index)
    }

    override fun getMaxItemCount(stack: ItemStack): Int {
        val maxAdd = stack.copy()
        val maxInput = stack.maxCount
        maxAdd.count = maxInput
        val currentStack = itemHandler.getStack(index)
        itemHandler.setStack(index, ItemStack.EMPTY)
        val remainder = itemHandler.insertStack(index, maxAdd, true)
        itemHandler.setStack(index, currentStack)
        return maxInput - remainder.count
    }

    override fun canTakeItems(playerEntity: PlayerEntity?): Boolean {
        return !itemHandler.takeStack(index, 1, true).isEmpty
    }

    override fun takeStack(amount: Int): ItemStack {
        return itemHandler.takeStack(index, amount, false)
    }
}