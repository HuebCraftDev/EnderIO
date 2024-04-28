package de.huebcraft.mods.enderio.conduits

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.block.ConduitBlock
import de.huebcraft.mods.enderio.conduits.init.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

internal object Main : ModInitializer {
    internal val LOGGER = LogManager.getLogger(BuildConstants.modName)

    override fun onInitialize() {
        LOGGER.info("Main has been initialized")
        ModBlocks.register()
        ModBlockEntities.register()
        ModItems.register()
        ModConduitTypes.register()
        EnderConduitTypes.register()

        PlayerBlockBreakEvents.BEFORE.register { world: World, playerEntity: PlayerEntity, blockPos: BlockPos, blockState: BlockState, blockEntity: BlockEntity? ->
            val conduitBlock = ModBlocks.CONDUIT() as ConduitBlock
            if (blockState.isOf(conduitBlock)) {
                return@register conduitBlock.canBreak(world, blockPos, blockState, playerEntity)
            }
            true
        }
        TODO("Packets")
    }
}