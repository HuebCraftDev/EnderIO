package de.huebcraft.mods.enderio.conduits.conduit

import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.client.model.ConduitRenderData
import de.huebcraft.mods.enderio.conduits.conduit.connection.ConduitConnection
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.ConduitTypeSorter
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*

/**
 * The data of a conduit block entity
 */
class ConduitBundle(val onDirty: () -> Unit, val pos: BlockPos) {
    companion object {
        const val MAX_TYPES = 9

        private const val KEY_TYPES = "Types"
        private const val KEY_CONNECTIONS = "Connections"
        private const val KEY_FACADES = "Facades"
        private const val KEY_NODE_TYPE = "NodeType"
        private const val KEY_NODE_DATA = "NodeData"
        private const val KEY_NODES = "Nodes"
    }

    private val connections = EnumMap<Direction, ConduitConnection>(Direction::class.java)

    val types = mutableListOf<IConduitType<*>>()

    private val nodes = mutableMapOf<IConduitType<*>, InWorldNode<*>>()

    var dataVersion = Int.MIN_VALUE
        private set

    init {
        for (direction in Direction.entries) {
            connections[direction] = ConduitConnection(this)
        }
    }

    fun <T : IExtendedConduitData<T>> addType(
        world: World, type: IConduitType<T>, player: PlayerEntity
    ): RightClickAction {
        if (types.size == MAX_TYPES || types.contains(type)) return RightClickAction.Blocked

        val oldType = types.firstOrNull { it.canBeReplacedBy(type) }
        val node = InWorldNode(pos, type.createExtendedConduitData(world, pos))
        if (oldType != null) {
            val oldTypeIndex = types.indexOf(oldType)
            types[oldTypeIndex] = type
            val oldNode = nodes.remove(oldType)
            nodes[type] = node
            if (oldNode != null) {
                oldNode.extendedConduitData.onRemoved(type, world, pos)
                if (!world.isClient) {
                    oldNode.getGraph()?.remove(oldNode)
                }
            }
            node.extendedConduitData.onCreated(type, world, pos, player)
            connections.values.forEach { it.disconnectType(oldTypeIndex) }
            onDirty()
            ++dataVersion
            return RightClickAction.Upgrade(oldType)
        }

        if (types.any { !it.canBeInSameBlock(type) || !type.canBeInSameBlock(it) }) return RightClickAction.Blocked

        val sortIndex = ConduitTypeSorter.getSortIndex(type)
        val insertAt = types.indexOfFirst { ConduitTypeSorter.getSortIndex(it) > sortIndex }
        if (insertAt != -1) {
            types.add(insertAt, type)
            nodes[type] = node
            node.extendedConduitData.onCreated(type, world, pos, player)

            for (direction in Direction.entries) {
                connections[direction]!!.addType(insertAt)
            }
        } else {
            types.add(type)
            nodes[type] = node
            if (types.size != 1) node.extendedConduitData.onCreated(type, world, pos, player)
        }

        onDirty()
        ++dataVersion
        return RightClickAction.Insert
    }

    protected fun onLoad(world: World, pos: BlockPos) {
        for (type in types) {
            getNodeFor(type).extendedConduitData.onCreated(type, world, pos, null)
        }
    }

    fun removeType(world: World, type: IConduitType<*>): Boolean {
        val idx = types.indexOf(type)
        if (idx == -1) return types.isEmpty()

        for (direction in Direction.entries) {
            connections[direction]!!.removeType(idx)
        }
        if (!world.isClient) removeNodeFor(world, type)

        types.removeAt(idx)
        onDirty()
        ++dataVersion
        return types.isEmpty()
    }

    fun serializeNbt(): NbtCompound {
        val nbt = NbtCompound()
        val listNbt = NbtList()
        for (type in types) {
            listNbt.add(NbtString.of(ConduitTypes.REGISTRY.getId(type).toString()))
        }
        nbt.put(KEY_TYPES, listNbt)
        val connectionsNbt = NbtCompound()
        for (direction in Direction.entries) {
            connectionsNbt.put(direction.getName(), connections[direction]!!.serializeNbt())
        }
        nbt.put(KEY_CONNECTIONS, connectionsNbt)
        // TODO Facades
        if (FabricLoader.getInstance().environmentType !== EnvType.SERVER) return nbt
        val nodeNbt = NbtList()
        for ((type, node) in nodes) {
            val data = node.extendedConduitData.serializeRenderNbt()
            if (!data.isEmpty) {
                val dataNbt = NbtCompound()
                dataNbt.putString(KEY_NODE_TYPE, ConduitTypes.REGISTRY.getId(type).toString())
                dataNbt.put(KEY_NODE_DATA, data)
                nodeNbt.add(dataNbt)
            }
        }
        if (!nodeNbt.isEmpty()) {
            nbt.put(KEY_NODES, nodeNbt)
        }
        return nbt
    }

    fun deserializeNbt(nbt: NbtCompound) {
        types.clear()
        val typesNbt = nbt.getList(KEY_TYPES, NbtElement.STRING_TYPE.toInt())
        val invalidTypes = mutableListOf<Int>()
        for ((i, typeId) in typesNbt.withIndex()) {
            val type = ConduitTypes.REGISTRY.get(Identifier.tryParse(typeId.asString()))
            if (type == null) {
                invalidTypes.add(i)
                continue
            }
            types.add(type)
        }
        val connectionsNbt = nbt.getCompound(KEY_CONNECTIONS)
        for (direction in Direction.entries) {
            connections[direction]!!.deserializeNbt(connectionsNbt.getCompound(direction.getName()))
            for (invalidType in invalidTypes) {
                connections[direction]!!.removeType(invalidType)
            }
            for (invalidType in invalidTypes.reversed()) {
                connections[direction]!!.removeType(invalidType)
            }
        }
        // TODO Facades
        nodes.entries.removeIf { !types.contains(it.key) }
        if (FabricLoader.getInstance().environmentType === EnvType.SERVER) {
            for (type in types) {
                if (nodes.containsKey(type)) {
                    for (direction in Direction.entries) {
                        val state =
                            getConnection(direction).getConnectionState(type) as? IConnectionState.DynamicConnectionState
                                ?: continue
                        ConduitBlockEntity.pushIOState(direction, nodes[type]!!, state)
                    }
                }
            }
        } else {
            types.forEach {
                if (!nodes.containsKey(it)) {
                    nodes[it] = InWorldNode.createUnsafe(
                        pos,
                        it.createExtendedConduitData(MinecraftClient.getInstance().world!!, pos)
                    )
                }
            }
            if (nbt.contains(KEY_NODES)) {
                val nodesNbt = nbt.getList(KEY_NODES, NbtElement.COMPOUND_TYPE.toInt())
                for (nodeNbt in nodesNbt) {
                    nodeNbt as NbtCompound
                    nodes[ConduitTypes.REGISTRY.get(Identifier(nodeNbt.getString(KEY_NODE_TYPE)))]!!.extendedConduitData.readNbt(
                        nodeNbt.getCompound(
                            KEY_NODE_DATA
                        )
                    )
                }
            }
        }
    }

    fun getConnection(direction: Direction): ConduitConnection = connections[direction]!!

    fun connectTo(world: World, pos: BlockPos, direction: Direction, type: IConduitType<*>, end: Boolean) {
        getConnection(direction).connectTo(world, pos, getNodeFor(type), direction, type, getTypeIndex(type), end)
        onDirty()
        ++dataVersion
    }

    fun disconnectFrom(direction: Direction, type: IConduitType<*>): Boolean {
        for ((i, bundleType) in types.withIndex()) {
            if (type.ticker.canConnectTo(type, bundleType)) {
                getConnection(direction).tryDisconnect(i)
                onDirty()
                ++dataVersion
                return true
            }
        }
        return false
    }

    fun getNodeForTypeExact(type: IConduitType<*>): InWorldNode<*>? = nodes[type]

    fun <T : IExtendedConduitData<T>> getNodeFor(type: IConduitType<T>): InWorldNode<T> {
        for ((key, value) in nodes) {
            @Suppress("UNCHECKED_CAST") if (key.ticker.canConnectTo(key, type)) return value as InWorldNode<T>
        }
        throw IllegalStateException("no node matching original type")
    }

    fun setNodeFor(type: IConduitType<*>, node: InWorldNode<*>) {
        nodes[type] = node
        for (direction in Direction.entries) {
            val connection = connections[direction]!!
            val index = types.indexOf(type)
            if (index >= 0) {
                val state = connection.getConnectionState(index) as? IConnectionState.DynamicConnectionState ?: continue
                ConduitBlockEntity.pushIOState(direction, node, state)
            }
        }
        ++dataVersion
    }

    fun removeNodeFor(world: World, type: IConduitType<*>) {
        val node = nodes[type]!!
        node.extendedConduitData.onRemoved(type, world, pos)
        node.getGraph()?.remove(node)
        nodes.remove(type)
        ++dataVersion
    }

    fun hasType(type: IConduitType<*>): Boolean {
        for (bundleType in types) {
            if (bundleType.ticker.canConnectTo(bundleType, type)) return true
        }
        return false
    }

    fun getTypeIndex(type: IConduitType<*>): Int {
        for ((i, bundleType) in types.withIndex()) {
            if (bundleType.ticker.canConnectTo(bundleType, type)) return i
        }
        throw IllegalStateException("no conduit matching type in bundle")
    }

    fun incrementDataVersion() {
        ++dataVersion
    }

    @Environment(EnvType.CLIENT)
    fun createRenderData(): ConduitRenderData {
        val connectionData = mutableMapOf<Direction, ConduitRenderData.ConnectionData>()
        val extendedData = mutableMapOf<IConduitType<*>, IExtendedConduitData<*>>()
        nodes.forEach { (type, node) -> extendedData[type] = node.extendedConduitData.deepCopy() }
        connections.forEach { (direction, connection) ->
            connectionData[direction] = connection.createConnectionData()
        }
        return ConduitRenderData(connectionData, extendedData, null)
    }
}