package de.huebcraft.mods.enderio.conduits.network

import de.huebcraft.mods.enderio.conduits.Main
import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.redstone.RedstoneExtendedData
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.GraphObject
import dev.gigaherz.graph3.Mergeable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.PersistentState
import java.io.File

class ConduitPersistentState private constructor() : PersistentState() {

    companion object {
        private const val KEY_GRAPHS = "Graphs"
        private const val KEY_TYPE = "Type"
        private const val KEY_GRAPH_OBJECTS = "GraphObjects"
        private const val KEY_GRAPH_CONNECTIONS = "GraphConnections"
        private const val KEY_DATA = "Data"

        fun get(world: ServerWorld): ConduitPersistentState = world.persistentStateManager.getOrCreate(
            { ConduitPersistentState(world, it) }, ::ConduitPersistentState, "enderio_conduit_network"
        )

        private fun serializeGraph(graph: Graph<Mergeable.Dummy>): NbtCompound {
            val objects = graph.objects
            val connections = mutableListOf<Pair<GraphObject<Mergeable.Dummy>, GraphObject<Mergeable.Dummy>>>()

            val nbt = NbtCompound()
            val graphObjectsNbt = NbtList()
            val graphConnectionsNbt = NbtList()
            for (graphObject in objects) {
                for (neighbor in graph.getNeighbours(graphObject)) {
                    val connection = graphObject to neighbor
                    if (!containsConnection(connections, connection)) connections.add(connection)
                }

                graphObject as InWorldNode<*>
                val dataNbt = NbtCompound()
                val posNbt = NbtCompound()
                dataNbt.put("BlockPos", posNbt)
                posNbt.putInt("x", graphObject.pos.x)
                posNbt.putInt("y", graphObject.pos.y)
                posNbt.putInt("z", graphObject.pos.z)
                dataNbt.put(KEY_DATA, graphObject.extendedConduitData.writeNbt())
                graphObjectsNbt.add(dataNbt)
            }

            for ((first, second) in connections) {
                val connectionNbt = NbtCompound()
                connectionNbt.putInt("0", objects.indexOf(first))
                connectionNbt.putInt("1", objects.indexOf(second))

                graphConnectionsNbt.add(connectionNbt)
            }

            nbt.put(KEY_GRAPH_OBJECTS, graphObjectsNbt)
            nbt.put(KEY_GRAPH_CONNECTIONS, graphConnectionsNbt)
            return nbt
        }

        private fun containsConnection(
            connections: List<Pair<GraphObject<Mergeable.Dummy>, GraphObject<Mergeable.Dummy>>>,
            connection: Pair<GraphObject<Mergeable.Dummy>, GraphObject<Mergeable.Dummy>>
        ): Boolean = connections.contains(connection) || connections.contains(connection.second to connection.first)

        private fun isRedstoneActive(world: ServerWorld, pos: BlockPos, colorControl: ColorControl): Boolean {
            if (!world.isChunkLoaded(pos) || !world.shouldTick(pos)) return false

            val conduit = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return false

            if (!conduit.bundle.types.contains(EnderConduitTypes.REDSTONE())) return false

            val data = conduit.bundle.getNodeFor(EnderConduitTypes.REDSTONE()).extendedConduitData
            return data.isActive(colorControl)
        }

        fun addPotentialGraph(type: IConduitType<*>, graph: Graph<Mergeable.Dummy>, world: ServerWorld) {
            get(world).addPotentialGraph(type, graph)
        }
    }

    private val networks = mutableMapOf<IConduitType<*>, MutableList<Graph<Mergeable.Dummy>>>()

    private val deserializedNodes =
        mutableMapOf<IConduitType<*>, MutableMap<ChunkPos, MutableMap<BlockPos, InWorldNode<*>>>>()

    private constructor(world: ServerWorld, nbt: NbtCompound) : this() {
        val graphs = nbt.getList(KEY_GRAPHS, NbtElement.COMPOUND_TYPE.toInt())
        for (compound in graphs) {
            compound as NbtCompound
            val type = Identifier(compound.getString(KEY_TYPE))
            val value = ConduitTypes.REGISTRY.get(type) ?: continue
            val graphsForType = compound.getList(KEY_GRAPHS, NbtElement.COMPOUND_TYPE.toInt())
            for (graphCompound in graphsForType) {
                graphCompound as NbtCompound
                val graphObjects = graphCompound.getList(KEY_GRAPH_OBJECTS, NbtElement.COMPOUND_TYPE.toInt())
                val graphConnections = graphCompound.getList(KEY_GRAPH_CONNECTIONS, NbtElement.COMPOUND_TYPE.toInt())

                val deserializedObjects = mutableListOf<InWorldNode<*>>()
                val connections = mutableListOf<Pair<GraphObject<Mergeable.Dummy>, GraphObject<Mergeable.Dummy>>>()

                for (graphObject in graphObjects) {
                    graphObject as NbtCompound
                    val pos = BlockEntity.posFromNbt(graphObject.getCompound("BlockPos"))
                    val node = InWorldNode.createUnsafe(pos, value.createExtendedConduitData(world, pos))
                    node.extendedConduitData.readNbt(graphObject.getCompound(KEY_DATA))
                    deserializedObjects.add(node)
                    putUnloadedNodeIdentifier(value, pos, node)
                }
                for (connection in graphConnections) {
                    connection as NbtCompound
                    connections.add(
                        deserializedObjects[connection.getInt("0")] to deserializedObjects[connection.getInt(
                            "1"
                        )]
                    )
                }

                val first = deserializedObjects.first()
                Graph.integrate(first, mutableListOf())
                merge(first, connections)

                networks.computeIfAbsent(value) { arrayListOf() }.add(first.getGraph()!!)
            }
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val graphsNbt = NbtList()
        for ((type, graphs) in networks) {
            if (graphs.isEmpty()) continue
            
            val typedGraphNbt = NbtCompound()
            typedGraphNbt.putString(KEY_TYPE, ConduitTypes.REGISTRY.getId(type).toString())

            val graphsForTypeNbt = NbtList()
            for (graph in graphs) {
                if (!graph.objects.isEmpty()) graphsForTypeNbt.add(serializeGraph(graph))
            }

            if (!graphsForTypeNbt.isEmpty()) {
                typedGraphNbt.put(KEY_GRAPHS, graphsForTypeNbt)
                graphsNbt.add(typedGraphNbt)
            }
        }
        nbt.put(KEY_GRAPHS, graphsNbt)
        return nbt
    }

    override fun isDirty(): Boolean = true

    private tailrec fun merge(
        obj: GraphObject<Mergeable.Dummy>,
        connections: List<Pair<GraphObject<Mergeable.Dummy>, GraphObject<Mergeable.Dummy>>>
    ) {
        val filteredConnections = connections.filter { it.first === obj || it.second === obj }

        for (neighbor in filteredConnections.map { if (it.first === obj) it.second else it.first }) {
            Graph.connect(obj, neighbor)
        }
        val remaining = connections.filter { !filteredConnections.contains(it) }
        if (remaining.isNotEmpty()) {
            merge(remaining.first().first, remaining)
        }
    }

    fun <T : IExtendedConduitData<T>> takeUnloadedNodeIdentifier(
        type: IConduitType<T>, pos: BlockPos
    ): InWorldNode<T>? {
        val chunkPos = ChunkPos(pos)

        val typeMap = deserializedNodes[type] ?: return null
        val chunkMap = typeMap[chunkPos] ?: return null
        val node = chunkMap[pos]

        chunkMap.remove(pos)
        if (chunkMap.isEmpty()) {
            typeMap.remove(chunkPos)
        }

        if (typeMap.isEmpty()) {
            deserializedNodes.remove(type)
        }

        @Suppress("UNCHECKED_CAST") return node as InWorldNode<T>
    }

    fun putUnloadedNodeIdentifier(type: IConduitType<*>, pos: BlockPos, node: InWorldNode<*>) {
        val chunkPos = ChunkPos(pos)
        deserializedNodes.computeIfAbsent(type) { mutableMapOf() }.computeIfAbsent(chunkPos) { mutableMapOf() }[pos] =
            node
    }

    fun serverTick(world: ServerWorld) {
        markDirty()
        for (graphs in networks.values) {
            graphs.removeIf { it.objects.isEmpty() || it.objects.iterator().next().graph !== it }
        }
        for ((type, graphs) in networks.entries) {
            for (graph in graphs) {
                if ((world.time % type.ticker.getTickRate()).toInt() == ConduitTypes.REGISTRY.getRawId(type) % type.ticker.getTickRate()) {
                    type.ticker.tickGraph(type, graph, world, ::isRedstoneActive)
                }
            }
        }
    }

    private fun addPotentialGraph(type: IConduitType<*>, graph: Graph<Mergeable.Dummy>) {
        val typeNetworks = networks.computeIfAbsent(type) { arrayListOf() }
        if (!typeNetworks.contains(graph))
            typeNetworks.add(graph)
    }

    override fun save(file: File) {
        if (isDirty) {
            val tmp = file.toPath().parent.resolve("${file.name}.tmp").toFile()
            super.save(tmp)
            if (file.exists() && !file.delete()) {
                Main.LOGGER.error("Failed to delete ${file.getName()}")
            }
            if (!tmp.renameTo(file)) {
                Main.LOGGER.error("Failed to rename ${tmp.getName()}")
            }
        }
    }
}