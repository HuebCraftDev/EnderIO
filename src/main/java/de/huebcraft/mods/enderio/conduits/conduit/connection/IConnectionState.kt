package de.huebcraft.mods.enderio.conduits.conduit.connection

import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

sealed interface IConnectionState {
    fun isConnection(): Boolean

    enum class StaticConnectionStates : IConnectionState {
        CONNECTED, CONNECTED_ACTIVE, DISCONNECTED, DISABLED;

        override fun isConnection(): Boolean = this === CONNECTED || this === CONNECTED_ACTIVE
    }

    data class DynamicConnectionState(
        val isInsert: Boolean,
        val insert: ColorControl,
        val isExtract: Boolean,
        val extract: ColorControl,
        val redstoneControl: RedstoneControl,
        val redstoneChannel: ColorControl,
        @Environment(EnvType.SERVER) val filterInsert: ItemStack,
        @Environment(EnvType.SERVER) val filterExtract: ItemStack,
        @Environment(EnvType.SERVER) val upgradeExtract: ItemStack
    ) : IConnectionState {

        companion object {
            fun defaultConnection(
                world: World, pos: BlockPos, direction: Direction, type: IConduitType<*>
            ): DynamicConnectionState {
                val default = type.getDefaultConnection(world, pos, direction)
                return DynamicConnectionState(
                    default.isInsert,
                    ColorControl.GREEN,
                    default.isExtract,
                    ColorControl.GREEN,
                    default.control,
                    ColorControl.RED,
                    ItemStack.EMPTY,
                    ItemStack.EMPTY,
                    ItemStack.EMPTY
                )
            }
        }

        override fun isConnection(): Boolean = true

        fun getItem(slotType: SlotType): ItemStack {
            return when (slotType) {
                SlotType.FILTER_EXTRACT -> filterExtract
                SlotType.FILTER_INSERT -> filterInsert
                SlotType.UPGRADE_EXTRACT -> upgradeExtract
            }
        }

        fun isEmpty(): Boolean = !isInsert && !isExtract
    }
}