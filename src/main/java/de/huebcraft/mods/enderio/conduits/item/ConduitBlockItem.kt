package de.huebcraft.mods.enderio.conduits.item

import de.huebcraft.mods.enderio.conduits.block.entity.ConduitBlockEntity
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.mixin.ItemUsageContextAccessor
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.world.event.GameEvent

class ConduitBlockItem(val typeSupplier: () -> IConduitType<*>, block: Block?, settings: Settings?) :
    BlockItem(block, settings) {
    override fun getTranslationKey(): String = orCreateTranslationKey

    override fun place(context: ItemPlacementContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val player = context.player
        val blockEntity = world.getBlockEntity(pos) as? ConduitBlockEntity
            ?: return super.place(context)
        if (blockEntity.hasType(typeSupplier())) {
            return world.getBlockState(pos).onUse(world, player, context.hand, (context as ItemUsageContextAccessor).invokeGetHitResult())
        }

        blockEntity.addType(typeSupplier(), player)
        if (world.isClient) blockEntity.updateClient()

        val state = world.getBlockState(pos)
        val soundGroup = state.soundGroup
        world.playSound(player, pos, getPlaceSound(state), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch() * 0.8f)
        world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(player, state))

        if (player?.abilities?.creativeMode == false) {
            context.stack.decrement(1)
        }
        return ActionResult.success(world.isClient)
    }
}