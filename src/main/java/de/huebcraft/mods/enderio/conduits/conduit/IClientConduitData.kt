package de.huebcraft.mods.enderio.conduits.conduit

import de.huebcraft.mods.enderio.conduits.misc.IIcon
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import org.joml.Vector2i
import java.util.function.Supplier

interface IClientConduitData<T : IExtendedConduitData<T>> : IIcon {
    override val iconSize: Vector2i
        get() = Vector2i(24, 24)
    override val renderSize: Vector2i
        get() = Vector2i(12, 12)

    fun emitConnectionQuads(
        extendedData: T, connectionDirection: Direction, randomSupplier: Supplier<Random>, context: RenderContext
    ) {}

    fun getModel(model: Identifier): BakedModel? = MinecraftClient.getInstance().bakedModelManager.getModel(model)

    class Simple<T : IExtendedConduitData<T>>(
        override val textureLocation: Identifier,
        override val texturePosition: Vector2i
    ) : IClientConduitData<T> {
    }
}