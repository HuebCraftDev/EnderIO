package de.huebcraft.mods.enderio.conduits.client.gui.screen

import com.mojang.blaze3d.systems.RenderSystem
import de.huebcraft.mods.enderio.conduits.misc.IIcon
import net.minecraft.client.gui.DrawContext
import org.joml.Vector2i
import org.joml.Vector2ic

interface IEnderScreen {
    companion object {
        fun renderIcon(context: DrawContext, pos: Vector2ic, icon: IIcon) {
            if (!icon.shouldRender()) {
                return
            }

            RenderSystem.enableBlend()
            context.drawTexture(
                icon.textureLocation,
                pos.x(),
                pos.y(),
                icon.renderSize.x(),
                icon.renderSize.y(),
                icon.texturePosition.x().toFloat(),
                icon.texturePosition.y().toFloat(),
                icon.iconSize.x(),
                icon.iconSize.y(),
                icon.textureSize.x(),
                icon.textureSize.y()
            )
            RenderSystem.disableBlend()
        }
    }

    fun renderSimpleArea(context: DrawContext, pos: Vector2ic, pos2: Vector2ic) {
        context.fill(pos.x(), pos.y(), pos2.x(), pos2.y(), -0x747475)
        context.fill(pos.x(), pos.y(), pos2.x() - 1, pos2.y() - 1, -0xc8c8c9)
        context.fill(pos.x() + 1, pos.y() + 1, pos2.x(), pos2.y(), -0x1)
        context.fill(pos.x() + 1, pos.y() + 1, pos2.x() - 1, pos2.y() - 1, -0x747475)
    }

    fun renderIconBackground(context: DrawContext, pos: Vector2ic, icon: IIcon) {
        renderSimpleArea(context, pos, pos.add(icon.renderSize, Vector2i()))
    }
}