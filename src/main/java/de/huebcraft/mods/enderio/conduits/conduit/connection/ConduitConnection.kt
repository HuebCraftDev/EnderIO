package de.huebcraft.mods.enderio.conduits.conduit.connection

import de.huebcraft.mods.enderio.conduits.Main
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.client.model.ConduitRenderData
import de.huebcraft.mods.enderio.conduits.conduit.ConduitBundle
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class ConduitConnection(val bundle: ConduitBundle) {
    private val connectionStates =
        Array<IConnectionState>(ConduitBundle.MAX_TYPES) { IConnectionState.StaticConnectionStates.DISCONNECTED }

    fun addType(index: Int) {
        for (i in ConduitBundle.MAX_TYPES - 1 downTo index + 1) {
            connectionStates[i] = connectionStates[i - 1]
        }
        connectionStates[index] = IConnectionState.StaticConnectionStates.DISCONNECTED
    }

    fun connectTo(
        world: World,
        pos: BlockPos,
        node: InWorldNode<*>,
        direction: Direction,
        type: IConduitType<*>,
        typeIndex: Int,
        end: Boolean
    ) {
        if (end) {
            val state = IConnectionState.DynamicConnectionState.defaultConnection(world, pos, direction, type)
            connectionStates[typeIndex] = state
            ConduitBlockEntity.pushIOState(direction, node, state)
        } else {
            connectionStates[typeIndex] = IConnectionState.StaticConnectionStates.CONNECTED
        }
    }

    fun tryDisconnect(typeIndex: Int) {
        if (connectionStates[typeIndex] != IConnectionState.StaticConnectionStates.DISABLED) {
            connectionStates[typeIndex] = IConnectionState.StaticConnectionStates.DISCONNECTED
        }
    }

    fun removeType(index: Int) {
        connectionStates[index] = IConnectionState.StaticConnectionStates.DISCONNECTED
        for (i in index + 1 until ConduitBundle.MAX_TYPES) {
            connectionStates[i - 1] = connectionStates[i]
        }
        connectionStates[ConduitBundle.MAX_TYPES - 1] = IConnectionState.StaticConnectionStates.DISCONNECTED
    }

    fun disconnectType(index: Int) {
        connectionStates[index] = IConnectionState.StaticConnectionStates.DISCONNECTED
    }

    fun disableType(index: Int) {
        connectionStates[index] = IConnectionState.StaticConnectionStates.DISABLED
        bundle.incrementDataVersion()
    }

    fun isEnd(): Boolean = connectionStates.any { it is IConnectionState.DynamicConnectionState }

    fun getConnectedTypes(): MutableList<IConduitType<*>> {
        connectionStates.filter { it.isConnection() }
        val connected = mutableListOf<IConduitType<*>>()
        for (i in connectionStates.indices) {
            if (connectionStates[i].isConnection()) {
                if (bundle.types.size <= i) {
                    for (connectionState in connectionStates) {
                        Main.LOGGER.info(connectionState)
                    }
                    Main.LOGGER.warn("Index out of bounds $i")
                    break
                }
                connected.add(bundle.types[i])
            }
        }
        return connected
    }

    companion object {
        private const val KEY_STATIC = "Static"
        private const val KEY_INDEX = "Index"
        private const val KEY_IS_EXTRACT = "IsExtract"
        private const val KEY_EXTRACT = "Extract"
        private const val KEY_IS_INSERT = "IsInsert"
        private const val KEY_INSERT = "Insert"
        private const val KEY_REDSTONE_CONTROL = "RedstoneControl"
        private const val KEY_REDSTONE_CHANNEL = "Channel"
    }

    fun serializeNbt(): NbtCompound {
        val nbt = NbtCompound()
        for (i in 0 until ConduitBundle.MAX_TYPES) {
            val element = NbtCompound()
            val state = connectionStates[i]
            element.putBoolean(KEY_STATIC, state is IConnectionState.StaticConnectionStates)
            if (state is IConnectionState.StaticConnectionStates) {
                element.putInt(KEY_INDEX, state.ordinal)
            } else if (state is IConnectionState.DynamicConnectionState) {
                element.putBoolean(KEY_IS_EXTRACT, state.isExtract)
                element.putInt(KEY_EXTRACT, state.extract.ordinal)
                element.putBoolean(KEY_IS_INSERT, state.isInsert)
                element.putInt(KEY_INSERT, state.insert.ordinal)
                element.putInt(KEY_REDSTONE_CONTROL, state.redstoneControl.ordinal)
                element.putInt(KEY_REDSTONE_CHANNEL, state.redstoneChannel.ordinal)
            }
            nbt.put(i.toString(), element)
        }
        return nbt
    }

    fun deserializeNbt(nbt: NbtCompound) {
        for (i in 0 until ConduitBundle.MAX_TYPES) {
            val element = nbt.getCompound(i.toString())
            if (element.getBoolean(KEY_STATIC)) {
                connectionStates[i] = IConnectionState.StaticConnectionStates.entries[element.getInt(KEY_INDEX)]
            } else {
                val isExtract = element.getBoolean(KEY_IS_EXTRACT)
                val extractIndex = element.getInt(KEY_EXTRACT)
                val isInsert = element.getBoolean(KEY_IS_INSERT)
                val insertIndex = element.getInt(KEY_INSERT)
                val redControl = element.getInt(KEY_REDSTONE_CONTROL)
                val redChannel = element.getInt(KEY_REDSTONE_CHANNEL)
                val prev = connectionStates[i] as? IConnectionState.DynamicConnectionState
                connectionStates[i] = IConnectionState.DynamicConnectionState(
                    isInsert,
                    ColorControl.entries[insertIndex],
                    isExtract,
                    ColorControl.entries[extractIndex],
                    RedstoneControl.entries[redControl],
                    ColorControl.entries[redChannel],
                    prev?.filterInsert ?: ItemStack.EMPTY,
                    prev?.filterExtract ?: ItemStack.EMPTY,
                    prev?.upgradeExtract ?: ItemStack.EMPTY
                )
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun createConnectionData(): ConduitRenderData.ConnectionData =
        ConduitRenderData.ConnectionData(connectionStates.mapIndexed { index, iConnectionState ->
            if (iConnectionState.isConnection()) return@mapIndexed bundle.types[index] to iConnectionState
            return@mapIndexed null
        }.filterNotNull())

    fun getConnectionState(index: Int): IConnectionState = connectionStates[index]

    fun getConnectionState(type: IConduitType<*>): IConnectionState = connectionStates[bundle.getTypeIndex(type)]

    fun setConnectionState(
        type: IConduitType<*>, state: IConnectionState
    ) = setConnectionState(bundle.getTypeIndex(type), state)

    private fun setConnectionState(index: Int, state: IConnectionState) {
        connectionStates[index] = state
        bundle.incrementDataVersion()
    }
}