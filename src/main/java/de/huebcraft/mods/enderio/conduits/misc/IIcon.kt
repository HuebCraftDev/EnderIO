package de.huebcraft.mods.enderio.conduits.misc

import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i

interface IIcon {
    companion object {
        val DEFAULT_TEXTURE_SIZE: Vector2i = Vector2i(256, 256)
    }

    val textureLocation: Identifier

    val iconSize: Vector2i

    val renderSize: Vector2i
        get() = iconSize

    val texturePosition: Vector2i

    val tooltip: Text
        get() = Text.empty()

    val textureSize: Vector2i
        get() = DEFAULT_TEXTURE_SIZE

    fun shouldRender(): Boolean = true
}