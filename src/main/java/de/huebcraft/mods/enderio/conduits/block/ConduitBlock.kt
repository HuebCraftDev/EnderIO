package de.huebcraft.mods.enderio.conduits.block

import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.RightClickAction
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import de.huebcraft.mods.enderio.conduits.init.ModBlockEntities
import de.huebcraft.mods.enderio.conduits.item.ConduitBlockItem
import de.huebcraft.mods.enderio.conduits.mixin.ItemAccessor
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.event.GameEvent

class ConduitBlock(settings: Settings) : BlockWithEntity(settings), Waterloggable, RedstoneEmitter {
    companion object {
        val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED
    }

    init {
        defaultState = stateManager.defaultState.with(WATERLOGGED, false)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    @Deprecated("Deprecated in Java")
    override fun canBucketPlace(state: BlockState?, fluid: Fluid?): Boolean = false

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(WATERLOGGED, ctx.world.getFluidState(ctx.blockPos).fluid === Fluids.WATER)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED)
    }

    @Deprecated("Deprecated in Java")
    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (state.contains(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }
        @Suppress("DEPRECATION") return super.getStateForNeighborUpdate(
            state, direction, neighborState, world, pos, neighborPos
        )
    }

    @Deprecated("Deprecated in Java")
    override fun getFluidState(state: BlockState): FluidState {
        @Suppress("DEPRECATION") return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(
            state
        )
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun neighborUpdate(
        state: BlockState, world: World, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean
    ) {
        val conduit = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return super.neighborUpdate(
            state, world, pos, sourceBlock, sourcePos, notify
        )
        conduit.updateConnections(world, pos, sourcePos, true)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ConduitBlockEntity(pos, state)

    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(
        state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext
    ): VoxelShape = (world.getBlockEntity(pos) as? ConduitBlockEntity)?.shape?.combinedShape ?: VoxelShapes.fullCube()

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult
    ): ActionResult {
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity !is ConduitBlockEntity) return super.onUse(state, world, pos, player, hand, hit)
        val handStack = player.getStackInHand(hand)
        if (!handStack.isEmpty) {
            var result = addConduit(blockEntity, player, handStack, world.isClient)
            if (result != null) return result

            result = handleYeta(blockEntity, player, handStack, hit, world.isClient)
            if (result != null) return result

            // TODO Facade

            if (player.getStackInHand(hand).item is ConduitBlockItem) return super.onUse(
                state, world, pos, player, hand, hit
            )
        }
        val result = handleScreen(blockEntity, player, hit, world.isClient)
        if (result != null) return result
        return super.onUse(state, world, pos, player, hand, hit)
    }

    private fun addConduit(
        blockEntity: ConduitBlockEntity, player: PlayerEntity, handStack: ItemStack, isClient: Boolean
    ): ActionResult? {
        val conduitBlockItem = handStack.item as? ConduitBlockItem ?: return null

        val action = blockEntity.addType(conduitBlockItem.typeSupplier(), player)
        if (action !is RightClickAction.Blocked) {
            blockEntity.world!!.setBlockState(blockEntity.pos, blockEntity.cachedState)
        }

        var result: ActionResult? = null
        if (action is RightClickAction.Upgrade) {
            if (!player.abilities.creativeMode) {
                handStack.decrement(1)
                player.inventory.offerOrDrop(handStack)
            }
            result = ActionResult.success(isClient)
        } else if (action is RightClickAction.Insert) {
            if (!player.abilities.creativeMode) {
                handStack.decrement(1)
            }
            result = ActionResult.success(isClient)
        }
        if (result == null) return null

        val world = blockEntity.world!!
        val pos = blockEntity.pos
        val state = blockEntity.cachedState
        val soundGroup = state.soundGroup
        world.playSound(
            player,
            pos,
            soundGroup.placeSound,
            SoundCategory.BLOCKS,
            (soundGroup.getVolume() + 1.0f) / 2.0f,
            soundGroup.getPitch() * 0.8f
        )
        world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(player, state))
        return result
    }

    private fun handleYeta(
        blockEntity: ConduitBlockEntity, player: PlayerEntity, stack: ItemStack, hit: BlockHitResult, isClient: Boolean
    ): ActionResult? {
        // TODO
        return null
    }

    // TODO handleShiftYeta

    private fun handleFacade(
        blockEntity: ConduitBlockEntity, player: PlayerEntity, stack: ItemStack, hit: BlockHitResult, isClient: Boolean
    ): ActionResult? {
        // TODO
        return null
    }

    private fun handleScreen(
        blockEntity: ConduitBlockEntity, player: PlayerEntity, hit: BlockHitResult, isClient: Boolean
    ): ActionResult? {
        // TODO
        return null
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        if (world !is ClientWorld) return super.getPickStack(world, pos, state)
        val conduit = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return super.getPickStack(world, pos, state)
        val player = MinecraftClient.getInstance().player!!
        if (state.getOrEmpty(WATERLOGGED).orElse(false)) {
            val hitResult = ItemAccessor.invokeRaycast(world, player, RaycastContext.FluidHandling.NONE)
            if (hitResult.type === HitResult.Type.MISS) return ItemStack.EMPTY

            if (hitResult.blockPos == pos) {
                return conduit.shape.getConduit(pos, hitResult)?.getConduitItem()?.defaultStack ?: super.getPickStack(
                    world, pos, state
                )
            }
        }

        return super.getPickStack(world, pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?, state: BlockState?, type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return checkType(type, ModBlockEntities.CONDUIT_BLOCK_ENTITY()) { world1, pos, state1, be ->
            be.tick(
                world1, pos, state1, be
            )
        }
    }

    override fun onPlaced(
        world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack
    ) {
        val item = itemStack.item as ConduitBlockItem
        if (placer !is PlayerEntity) return
        val be = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return
        be.addType(item.typeSupplier(), placer)
        if (!world.isClient) be.updateClient()
    }

    fun canBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): Boolean {
        val hit = player.raycast(MinecraftClient.getInstance().interactionManager!!.reachDistance + 5.0, 1f, false)
        val be = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return true
        val type = be.shape.getConduit((hit as BlockHitResult).blockPos, hit)
        if (type == null) {
            if (be.bundle.types.isNotEmpty()) {
                world.playSound(player, pos, SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.BLOCKS, 1f, 1f)
                return false
            }
            return true
        }
        if (be.removeType(type, !player.abilities.creativeMode)) {
            return true
        }
        val soundGroup = state.soundGroup
        world.playSound(
            player,
            pos,
            soundGroup.breakSound,
            SoundCategory.BLOCKS,
            (soundGroup.getVolume() + 1.0f) / 2.0f,
            soundGroup.getPitch() * 0.8f
        )
        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state))
        return false
    }

    // Tell blocks that don't use enderio$emitsRedstone() that this block might emit redstone, even if there is no redstone conduit
    override fun emitsRedstonePower(state: BlockState?): Boolean {
        return true
    }

    override fun `enderio$emitsRedstone`(
        state: BlockState, world: BlockView, pos: BlockPos, direction: Direction?
    ): Boolean {
        // FIXME connection state not set
        val be = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return false
        return direction != null && be.bundle.types.contains(EnderConduitTypes.REDSTONE()) && be.bundle.getConnection(
            direction.opposite
        ).getConnectionState(EnderConduitTypes.REDSTONE()) is IConnectionState.DynamicConnectionState
    }

    @Deprecated("Deprecated in Java")
    override fun getWeakRedstonePower(
        state: BlockState, world: BlockView, pos: BlockPos, direction: Direction
    ): Int {
        val be = world.getBlockEntity(pos) as? ConduitBlockEntity ?: return 0
        if (!be.bundle.types.contains(EnderConduitTypes.REDSTONE())) return 0
        val connectionState =
            be.bundle.getConnection(direction.opposite).getConnectionState(EnderConduitTypes.REDSTONE())
        if (connectionState is IConnectionState.DynamicConnectionState && connectionState.isInsert && be.bundle.getNodeFor(
                EnderConduitTypes.REDSTONE()
            ).extendedConduitData.isActive(connectionState.insert)
        ) return 15
        return 0
    }
}