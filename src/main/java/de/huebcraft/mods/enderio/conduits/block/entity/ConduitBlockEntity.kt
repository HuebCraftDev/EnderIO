package de.huebcraft.mods.enderio.conduits.block.entity

import de.huebcraft.mods.enderio.conduits.client.model.ConduitRenderData
import de.huebcraft.mods.enderio.conduits.conduit.*
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ModBlockEntities.CONDUIT_BLOCK_ENTITY
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.network.ConduitBundleDataSlot
import de.huebcraft.mods.enderio.conduits.network.ConduitPersistentState
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.GraphObject
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.function.BiFunction

class ConduitBlockEntity(
    pos: BlockPos, state: BlockState
) : LazyBlockEntity(CONDUIT_BLOCK_ENTITY(), pos, state), BlockEntityTicker<ConduitBlockEntity>,
    ExtendedScreenHandlerFactory {
    val bundle = ConduitBundle(::markDirty, pos)
    val shape = ConduitShape()

    @Environment(EnvType.CLIENT)
    var clientData: ConduitRenderData = bundle.createRenderData()
    var checkConnection = UpdateState.NONE
    var lazyNodeNbt: NbtList = NbtList()
    val lazyNodes = mutableMapOf<IConduitType<*>, InWorldNode<*>>()

    init {
        addDataSlot(ConduitBundleDataSlot { bundle })
        addAfterSyncRunnable { updateClient() }
    }

    fun updateClient() {
        clientData = bundle.createRenderData()
        updateShape()
        world?.scheduleBlockRerenderIfNeeded(pos, Blocks.AIR.defaultState, cachedState)
    }

    fun isMenuValid(player: PlayerEntity): Boolean {
        if (world!!.getBlockEntity(pos) !== this) return false
        return player.eyePos.squaredDistanceTo(Vec3d.ofCenter(pos)) < ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE + 2.25f
    }

    override fun markRemoved() {
        val serverWorld = world as? ServerWorld
        if (serverWorld != null) {
            val persistentState = ConduitPersistentState.get(serverWorld)
            for (type in bundle.types) {
                val node = bundle.getNodeFor(type)
                node.extendedConduitData.onRemoved(type, serverWorld, pos)
                persistentState.putUnloadedNodeIdentifier(type, pos, node)
            }
        }
        super.markRemoved()
    }

    override fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: ConduitBlockEntity) {
        if (!world.isClient) {
            serverTick(world, pos)
            checkConnection = checkConnection.next()
            if (checkConnection.isInitialized) {
                updateConnections(world, pos, null, false)
            }
        }
    }

    fun updateConnections(world: World, pos: BlockPos, fromPos: BlockPos?, shouldActivate: Boolean) {
        for (direction in Direction.entries) {
            if (fromPos != null && world.getBlockEntity(fromPos) is ConduitBlockEntity) continue

            for (type in bundle.types) {
                if (shouldActivate && type.ticker.hasConnectionDelay()) {
                    checkConnection = checkConnection.activate()
                }
                val connectionState = bundle.getConnection(direction)
                    .getConnectionState(type)
                if (connectionState is IConnectionState.DynamicConnectionState) {
                    if (type.ticker.canConnectTo(world, pos, direction)) continue
                    bundle.getNodeFor(type).clearState(direction)
                    dropConnection(connectionState)
                    bundle.getConnection(direction)
                        .setConnectionState(type, IConnectionState.StaticConnectionStates.DISCONNECTED)
                    updateShape()
                    updateConnectionToData(type)
                } else if (connectionState === IConnectionState.StaticConnectionStates.DISCONNECTED) {
                    tryConnectTo(direction, type, forceMerge = true, shouldMergeGraph = true)
                }
            }
        }
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        bundle.deserializeNbt(nbt.getCompound("ConduitBundle"))
        lazyNodeNbt = nbt.getList("ConduitExtraData", NbtElement.COMPOUND_TYPE.toInt())
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.put("ConduitBundle", bundle.serializeNbt())
        val list = NbtList()
        for (type in bundle.types) {
            val data = bundle.getNodeFor(type).extendedConduitData
            list.add(data.writeNbt())
        }
        nbt.put("ConduitExtraData", list)
    }

    override fun setWorld(world: World) {
        super.setWorld(world)
        if (!world.isClient) loadFromSaveData()
        TODO("onLoad()")
    }

    override fun getRenderData(): Any = clientData

    fun hasType(type: IConduitType<*>): Boolean = bundle.hasType(type)

    fun <T : IExtendedConduitData<T>> addType(type: IConduitType<T>, player: PlayerEntity): RightClickAction {
        val action = bundle.addType(world!!, type, player)
        if (!action.hasChanged()) return action

        val nodes = mutableListOf<GraphObject<Mergeable.Dummy>>()
        for (direction in Direction.entries) {
            tryConnectTo(direction, type, false, shouldMergeGraph = false)?.apply {
                nodes.add(this)
            }
        }
        if (world is ServerWorld) {
            @Suppress("UNCHECKED_CAST") val thisNode = bundle.getNodeForTypeExact(type)!! as InWorldNode<T>
            Graph.integrate(thisNode, nodes)
            for (obj in thisNode.getGraph()!!.objects) {
                @Suppress("UNCHECKED_CAST") if (obj is InWorldNode<*>) thisNode.extendedConduitData.onConnectTo(obj.extendedConduitData as T)
            }
            ConduitPersistentState.addPotentialGraph(type, thisNode.getGraph()!!, world as ServerWorld)
        }
        if (action is RightClickAction.Upgrade && !action.notInConduit.ticker.canConnectTo(action.notInConduit, type)) {
            removeNeighborConnections(action.notInConduit)
        }
        updateShape()
        return action
    }

    fun <T : IExtendedConduitData<T>> tryConnectTo(
        direction: Direction, type: IConduitType<T>, forceMerge: Boolean, shouldMergeGraph: Boolean
    ): GraphObject<Mergeable.Dummy>? {
        val otherEntity = world!!.getBlockEntity(pos.offset(direction))
        if (otherEntity !is ConduitBlockEntity || !otherEntity.connectTo(
                direction.opposite, type, bundle.getNodeFor(type).extendedConduitData, forceMerge
            )
        ) {
            if (type.ticker.canConnectTo(world!!, pos, direction)) {
                connectEnd(direction, type)
                updateConnectionToData(type)
            }
            return null
        }

        connect(direction, type)
        updateConnectionToData(type)
        otherEntity.updateConnectionToData(type)
        val firstNode = otherEntity.bundle.getNodeFor(type)
        val secondNode = bundle.getNodeFor(type)
        firstNode.extendedConduitData.onConnectTo(secondNode.extendedConduitData)
        if (firstNode.getGraph() != null) {
            for (obj in firstNode.getGraph()!!.objects) {
                @Suppress("UNCHECKED_CAST") if (obj is InWorldNode<*> && obj != firstNode) firstNode.extendedConduitData.onConnectTo(
                    obj.extendedConduitData as T
                )
            }
        }
        if (secondNode.getGraph() != null && firstNode.getGraph() !== secondNode.getGraph()) {
            for (obj in secondNode.getGraph()!!.objects) {
                @Suppress("UNCHECKED_CAST") if (obj is InWorldNode<*> && obj != secondNode) secondNode.extendedConduitData.onConnectTo(
                    obj.extendedConduitData as T
                )
            }
        }
        if (shouldMergeGraph) {
            Graph.connect(bundle.getNodeFor(type), otherEntity.bundle.getNodeFor(type))
        }
        return otherEntity.bundle.getNodeFor(type)
    }

    fun updateConnectionToData(type: IConduitType<*>) {
        if (world!!.isClient) return
        bundle.getNodeFor(type).extendedConduitData.updateConnection(Direction.entries.filter {
            bundle.getConnection(it).getConnectionState(type) !== IConnectionState.StaticConnectionStates.DISABLED
        }.toSet())
    }

    fun removeTypeAndDelete(type: IConduitType<*>) {
        if (removeType(type, false)) {
            world!!.setBlockState(
                pos, cachedState.fluidState.blockState, if (world!!.isClient) Block.field_31022 else Block.NOTIFY_ALL
            )
        }
    }

    fun removeType(type: IConduitType<*>, shouldDrop: Boolean): Boolean {
        if (shouldDrop && !world!!.isClient) {
            dropItem(type.getConduitItem().defaultStack)
            for (direction in Direction.entries) {
                val connectionState = bundle.getConnection(direction).getConnectionState(type)
                if (connectionState is IConnectionState.DynamicConnectionState) dropConnection(connectionState)
            }
        }
        val shouldRemove = bundle.removeType(world!!, type)
        removeNeighborConnections(type)
        updateShape()
        return shouldRemove
    }

    fun updateEmptyDynConnection() {
        for (direction in Direction.entries) {
            val connection = bundle.getConnection(direction)
            for (i in 0 until ConduitBundle.MAX_TYPES) {
                val state = connection.getConnectionState(i) as? IConnectionState.DynamicConnectionState ?: continue
                if (state.isEmpty()) {
                    dropConnection(state)
                    connection.disableType(i)
                }
            }
        }
    }

    fun dropConnection(state: IConnectionState.DynamicConnectionState) {
        for (slotType in SlotType.entries) {
            val item = state.getItem(slotType)
            if (!item.isEmpty) dropItem(item)
        }
    }

    private fun dropItem(stack: ItemStack) {
        world!!.spawnEntity(ItemEntity(world!!, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack))
    }

    fun removeNeighborConnections(type: IConduitType<*>) {
        for (direction in Direction.entries) {
            val conduit = world!!.getBlockEntity(pos.offset(direction)) as? ConduitBlockEntity ?: continue
            conduit.disconnect(direction.opposite, type)
        }

        if (world !is ServerWorld) return
        for (direction in Direction.entries) {
            val conduit = world!!.getBlockEntity(pos.offset(direction)) as? ConduitBlockEntity ?: continue
            if (conduit.hasType(type)) {
                conduit.bundle.getNodeFor(type).getGraph()
                    ?.apply { ConduitPersistentState.addPotentialGraph(type, this, world as ServerWorld) }
            }
        }
    }

    fun updateShape() {
        shape.updateConduit(bundle)
    }

    private fun loadFromSaveData() {
        val serverWorld = world as? ServerWorld ?: return

        val savedData = ConduitPersistentState.get(serverWorld)
        for ((typeIndex, type) in bundle.types.withIndex()) {
            var node: InWorldNode<*>? = savedData.takeUnloadedNodeIdentifier(type, pos)
            if (node == null && bundle.getNodeForTypeExact(type) == null) {
                val data = type.createExtendedConduitData(serverWorld, pos)
                if (typeIndex < lazyNodeNbt.size) {
                    data.readNbt(lazyNodeNbt.getCompound(typeIndex))
                }
                node = InWorldNode.createUnsafe(pos, data)
                for (direction in Direction.entries) {
                    val connectionState = bundle.getConnection(direction).getConnectionState(type)
                    if (connectionState is IConnectionState.DynamicConnectionState) {
                        pushIOState(direction, node, connectionState)
                    }
                }
                Graph.integrate(node, mutableListOf())
                bundle.setNodeFor(type, node)
                lazyNodes[type] = node
            } else if (node != null) {
                bundle.setNodeFor(type, node)
            }
        }
        lazyNodeNbt.clear()
    }

    private fun <T : IExtendedConduitData<T>> connectTo(
        direction: Direction, type: IConduitType<T>, data: IExtendedConduitData<T>, forceMerge: Boolean
    ): Boolean {
        if (!doTypesMismatch(type)) return false

        if (!data.canConnectTo(bundle.getNodeFor(type).extendedConduitData)) return false

        if (forceMerge || bundle.getConnection(direction)
                .getConnectionState(type) !== IConnectionState.StaticConnectionStates.DISABLED
        ) {
            connect(direction, type)
            return true
        }
        return false
    }

    private fun doTypesMismatch(type: IConduitType<*>): Boolean {
        for (bundleType in bundle.types) {
            if (bundleType.ticker.canConnectTo(bundleType, type)) return true
        }
        return false
    }

    private fun connect(direction: Direction, type: IConduitType<*>) {
        bundle.connectTo(world!!, pos, direction, type, false)
        updateClient()
    }

    private fun connectEnd(direction: Direction, type: IConduitType<*>) {
        bundle.connectTo(world!!, pos, direction, type, true)
        updateClient()
    }

    private fun disconnect(direction: Direction, type: IConduitType<*>) {
        if (bundle.disconnectFrom(direction, type)) updateClient()
    }


    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T, Z : IExtendedConduitData<Z>> createConduitLookup(lookup: BlockApiLookup<T, Direction?>): BiFunction<ConduitBlockEntity, Direction?, T?> {
            return BiFunction { entity, dir ->
                for (type in entity.bundle.types) {
                    type as IConduitType<Z>
                    val node = entity.bundle.getNodeFor(type)
                    var state: InWorldNode.IOState? = null
                    if (dir != null) {
                        state = node.getIOState(dir)
                    }
                    val proxiedLookup = type.proxyLookup(
                        lookup, node.extendedConduitData, entity.world!!, entity.pos, dir, state
                    ) ?: continue
                    return@BiFunction proxiedLookup
                }
                null
            }
        }

        fun pushIOState(
            direction: Direction, node: InWorldNode<*>, connectionState: IConnectionState.DynamicConnectionState
        ) {
            node.pushState(
                direction,
                if (connectionState.isInsert) connectionState.insert else null,
                if (connectionState.isExtract) connectionState.extract else null,
                connectionState.redstoneControl,
                connectionState.redstoneChannel
            )
        }
    }

    enum class UpdateState {
        NONE, NEXT_NEXT, NEXT, INITIALIZED;

        val isInitialized: Boolean
            get() = this == INITIALIZED

        fun next(): UpdateState {
            return when (this) {
                NONE, INITIALIZED -> NONE
                NEXT_NEXT -> NEXT
                NEXT -> INITIALIZED
            }
        }

        fun activate(): UpdateState = NEXT_NEXT
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): Text {
        TODO("Not yet implemented")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf?) {
        TODO("Not yet implemented")
    }
}