package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.OffsetHelper
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.Area
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

@Environment(EnvType.CLIENT)
class ConduitBlockModel : BakedModel, UnbakedModel {
    override fun isVanillaAdapter(): Boolean = false

    companion object {
        val CONDUIT_CORE = Identifier(BuildConstants.modId, "block/conduit_core")
        val BOX = Identifier(BuildConstants.modId, "block/box/1x1x1")
        val CONDUIT_CONNECTOR = Identifier(BuildConstants.modId, "block/conduit_connector")
        val CONDUIT_FACADE = Identifier(BuildConstants.modId, "block/conduit_facade")
        val CONDUIT_CONNECTION = Identifier(BuildConstants.modId, "block/conduit_connection")
        val CONDUIT_CONNECTION_BOX = Identifier(BuildConstants.modId, "block/conduit_connection_box")
        val CONDUIT_IO_IN_OUT = Identifier(BuildConstants.modId, "block/io/in_out")
        val CONDUIT_IO_IN = Identifier(BuildConstants.modId, "block/io/input")
        val CONDUIT_IO_OUT = Identifier(BuildConstants.modId, "block/io/output")
        val CONDUIT_IO_REDSTONE = Identifier(BuildConstants.modId, "block/io/redstone")

        private fun <T : IExtendedConduitData<T>> sprite(type: IConduitType<T>, data: T) =
            MinecraftClient.getInstance().bakedModelManager.getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .getSprite(type.getTexture(data))

        private fun findMainAxis(renderData: ConduitRenderData): Axis {
            val connectedDirections = mutableListOf<Direction>()
            for (direction in Direction.entries) {
                val connectedTypes = renderData.connectionData[direction]!!.connectionStates
                if (connectedTypes.isNotEmpty()) connectedDirections.add(direction)
            }
            if (connectedDirections.isEmpty()) return Axis.Z

            return connectedDirections[connectedDirections.size - 1].axis
        }
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        val renderData = blockView.getBlockEntityRenderData(pos) as? ConduitRenderData ?: return
        val modelManager = MinecraftClient.getInstance().bakedModelManager
        val offsets = mutableMapOf<IConduitType<*>, MutableList<Vec3i>>()
        val axis = findMainAxis(renderData)
        for (direction in Direction.entries) {
            val connectionData = renderData.connectionData[direction] ?: continue
            val hasConnection = connectionData.connectionStates.any { it.second.staticConnection == null }
            if (hasConnection) {
                // TODO Rotate
                modelManager.getModel(CONDUIT_CONNECTOR)!!
                    .emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }
            for ((i, connection) in connectionData.connectionStates.withIndex()) {
                val offset = OffsetHelper.translationFor(
                    direction.axis, OffsetHelper.offsetConduit(i, connectionData.connectionStates.size)
                )
                offsets.computeIfAbsent(connection.first) { arrayListOf() }.add(offset)
                // TODO Transformations
                modelManager.getModel(CONDUIT_CONNECTION)!!
                    .emitBlockQuads(blockView, state, pos, randomSupplier, context)
                clientDataEmitQuadsUnsafe(
                    connection.first.clientData,
                    renderData.extendedData[connection.first]!!,
                    direction,
                    randomSupplier,
                    context
                )

                if (!hasConnection) continue

                // TODO Transformations
                modelManager.getModel(CONDUIT_CONNECTION_BOX)!!
                    .emitBlockQuads(blockView, state, pos, randomSupplier, context)

                if (connection.second.staticConnection != null) continue

                val insert = connection.second.insert
                val extract = connection.second.extract
                val model =
                    if (insert != null && extract != null) CONDUIT_IO_IN_OUT else if (insert != null) CONDUIT_IO_IN else if (extract != null) CONDUIT_IO_OUT else null
                if (model != null) {
                    // TODO Color
                    modelManager.getModel(model)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
                }
                if (connection.second.control !== RedstoneControl.ACTIVE_WITH_SIGNAL && connection.second.control !== RedstoneControl.ACTIVE_WITHOUT_SIGNAL) continue

                // TODO Color
                modelManager.getModel(CONDUIT_IO_REDSTONE)!!
                    .emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }

            if (renderData.facades != null) {
                val facade = MinecraftClient.getInstance().blockRenderManager.getModel(renderData.facades[direction])!!
                // TODO Transform
                modelManager.getModel(CONDUIT_FACADE)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }
        }

        val allTypes = renderData.extendedData.keys
        var box: Area? = null
        val notRendered = mutableMapOf<IConduitType<*>, Int>()
        val rendered = mutableListOf<IConduitType<*>>()
        for ((i, type) in allTypes.withIndex()) {
            val offsetsForType = offsets[type]
            if (offsetsForType == null) {
                notRendered[type] = i
                continue
            }
            if (offsetsForType.distinct().count() == 1) {
                rendered.add(type)
            } else {
                if (box == null) {
                    box = Area(offsetsForType)
                } else {
                    offsetsForType.forEach(box::makeContain)
                }
            }
        }
        val duplicateFinder = mutableSetOf<Vec3i>()
        val duplicatePositions = rendered.map { offsets[it]!![0] }.filter { !duplicateFinder.add(it) }
        for (duplicate in duplicatePositions) {
            if (box == null) {
                box = Area(duplicate)
            } else {
                box.makeContain(duplicate)
            }
        }
        for (toRender in rendered) {
            val offsetsForType = offsets[toRender]!!
            if (box == null || !box.contains(offsetsForType[0])) {
                // TODO transform
                modelManager.getModel(CONDUIT_CORE)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }
        }
        if (box != null) {
            for ((type, i) in notRendered) {
                val offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allTypes.size))
                if (!box.contains(offset)) {
                    // TODO transform
                    modelManager.getModel(CONDUIT_CORE)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
                }
            }
            // TODO transform
            modelManager.getModel(BOX)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        } else {
            for ((type, i) in notRendered) {
                // TODO transform
                modelManager.getModel(CONDUIT_CORE)!!.emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }
        }
    }

    private fun <T : IExtendedConduitData<T>> clientDataEmitQuadsUnsafe(
        clientData: IClientConduitData<T>,
        extendedData: IExtendedConduitData<*>,
        direction: Direction,
        randomSupplier: Supplier<Random>,
        context: RenderContext
    ) = clientData.emitConnectionQuads(extendedData as T, direction, randomSupplier, context)

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = false

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite =
        MinecraftClient.getInstance().bakedModelManager.getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            .getSprite(MissingSprite.getMissingSpriteId())

    override fun getTransformation(): ModelTransformation = ModelTransformation.NONE

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY
    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf(
        CONDUIT_CONNECTOR,
        CONDUIT_FACADE,
        CONDUIT_CONNECTION,
        CONDUIT_CONNECTION_BOX,
        CONDUIT_IO_IN_OUT,
        CONDUIT_IO_IN,
        CONDUIT_IO_OUT,
        CONDUIT_IO_REDSTONE
    )

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {
    }

    override fun bake(
        baker: Baker?,
        textureGetter: Function<SpriteIdentifier, Sprite>?,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel = this
}