package de.huebcraft.mods.enderio.conduits.screen

import de.huebcraft.mods.enderio.conduits.Main
import de.huebcraft.mods.enderio.conduits.block.ConduitBlock
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.ConduitBundle
import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitScreenHandlers
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction

class ConduitScreenHandler(
    val blockEntity: ConduitBlockEntity?,
    val playerInventory: PlayerInventory,
    syncId: Int,
    var direction: Direction,
    var conduitType: IConduitType<*>
) : ScreenHandler(ConduitScreenHandlers.CONDUIT_SCREEN_HANDLER(), syncId) {
    val conduitSlots = mutableListOf<ConduitSlot>()
    private val playerInvSlots = mutableListOf<Slot>()

    init {
        if (blockEntity != null) {
            val itemHandler = blockEntity.getItemHandler()
            for (direction in Direction.entries) {
                for (i in 0 until ConduitBundle.MAX_TYPES) {
                    for (slotType in SlotType.entries) {
                        val slot = ConduitSlot(
                            blockEntity.bundle,
                            itemHandler,
                            { this.direction },
                            direction,
                            { blockEntity.bundle.types.indexOf(this.conduitType) },
                            i,
                            slotType
                        )
                        conduitSlots.add(slot)
                        slot.updateVisibilityPosition()
                        addSlot(slot)
                    }
                }
            }
        }
        addInventorySlots(23, 113)
    }

    private fun addInventorySlots(xPos: Int, yPos: Int) {
        for (x in 0..8) {
            val slot = Slot(playerInventory, x, xPos + x * 18, yPos + 58)
            playerInvSlots.add(slot)
            addSlot(slot)
        }

        for (y in 0..2) {
            for (x in 0..8) {
                val slot = Slot(playerInventory, x + y * 9 + 9, xPos + x * 18, yPos + y * 18)
                playerInvSlots.add(slot)
                addSlot(slot)
            }
        }
    }

    companion object {
        fun factory(syncId: Int, playerInventory: PlayerInventory, buf: PacketByteBuf): ConduitScreenHandler {
            val be = playerInventory.player.world.getBlockEntity(buf.readBlockPos())
            val direction = buf.readEnumConstant(Direction::class.java)
            val conduitType = ConduitTypes.REGISTRY.getOrThrow(buf.readInt())
            if (be is ConduitBlockEntity) return ConduitScreenHandler(
                be, playerInventory, syncId, direction, conduitType
            )

            Main.LOGGER.warn("couldn't find BlockEntity")
            return ConduitScreenHandler(null, playerInventory, syncId, direction, conduitType)
        }
    }

    override fun quickMove(player: PlayerEntity, slotIdx: Int): ItemStack {
        val slot = slots[slotIdx]
        if (slot.hasStack()) {
            val stack = slot.stack
            val copied = stack.copy()
            if (slotIdx < slots.size - 36) {
                if (!this.insertItem(stack, slots.size - 36, slots.size, true))
                    return ItemStack.EMPTY
            } else if (!this.insertItem(stack, 0, slots.size - 36, false)) {
                return ItemStack.EMPTY
            }
            if (stack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            return copied
        }
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity): Boolean =
        blockEntity != null && blockEntity.isMenuValid(player) && (player is ServerPlayerEntity || clientValid())

    private fun clientValid(): Boolean =
        blockEntity!!.bundle.types.contains(conduitType) && ConduitBlock.canBeOrIsValidConnection(
            blockEntity, conduitType, direction
        )

    override fun onClosed(player: PlayerEntity) {
        super.onClosed(player)
        if (blockEntity != null && player is ServerPlayerEntity && player.serverWorld.players.filter { it != player }.none { it.currentScreenHandler is ConduitScreenHandler }) {
            blockEntity.updateEmptyDynConnection()
        }
    }
}