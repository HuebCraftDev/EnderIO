package de.huebcraft.mods.enderio.conduits.screen

import net.minecraft.item.ItemStack

interface ItemHandler {
    val slots: Int

    fun canInsert(index: Int, stack: ItemStack): Boolean

    fun getStack(index: Int): ItemStack

    fun setStack(index: Int, stack: ItemStack)

    fun getMaxItemCount(index: Int): Int

    fun insertStack(index: Int, stack: ItemStack, simulated: Boolean): ItemStack

    fun takeStack(index: Int, amount: Int, simulated: Boolean): ItemStack
}