package de.huebcraft.mods.enderio.conduits.conduit.connection

import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
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
        val filterInsert: ItemStack,
        val filterExtract: ItemStack,
        val upgradeExtract: ItemStack
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

            fun fromNetwork(buf: PacketByteBuf): DynamicConnectionState {
                val isInsert: Boolean = buf.readBoolean()
                val insert: ColorControl = buf.readEnumConstant(ColorControl::class.java)
                val isExtract: Boolean = buf.readBoolean()
                val extract: ColorControl = buf.readEnumConstant(ColorControl::class.java)
                val control: RedstoneControl = buf.readEnumConstant(RedstoneControl::class.java)
                val redstoneChannel: ColorControl = buf.readEnumConstant(ColorControl::class.java)
                val filterInsert: ItemStack = buf.readItemStack()
                val filterExtract: ItemStack = buf.readItemStack()
                val upgradeInsert: ItemStack = buf.readItemStack()
                return DynamicConnectionState(
                    isInsert,
                    insert,
                    isExtract,
                    extract,
                    control,
                    redstoneChannel,
                    filterInsert,
                    filterExtract,
                    upgradeInsert
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

        fun toNetwork(buf: PacketByteBuf) {
            buf.writeBoolean(isInsert)
            buf.writeEnumConstant(insert)
            buf.writeBoolean(isExtract)
            buf.writeEnumConstant(extract)
            buf.writeEnumConstant(redstoneControl)
            buf.writeEnumConstant(redstoneChannel)
            buf.writeItemStack(filterInsert)
            buf.writeItemStack(filterExtract)
            buf.writeItemStack(upgradeExtract)
        }
    }
}