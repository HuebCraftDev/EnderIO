package de.huebcraft.mods.enderio.conduits.client.gui.widget

import com.mojang.blaze3d.systems.RenderSystem
import de.huebcraft.mods.enderio.build.BuildConstants
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.joml.Vector2i

class CheckBox(
    private val texture: Identifier,
    pos: Vector2i,
    private val getter: () -> Boolean,
    private val setter: (Boolean) -> Unit,
    private val enabledTooltip: () -> Text?,
    private val disabledTooltip: () -> Text?
) : PressableWidget(pos.x, pos.y, 14, 14, Text.empty()) {
    companion object {
        val TEXTURE = Identifier(BuildConstants.modId, "textures/gui/checkbox.png")
    }

    constructor(pos: Vector2i, getter: () -> Boolean, setter: (Boolean) -> Unit) : this(TEXTURE,
        pos,
        getter,
        setter,
        { null },
        { null })

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        var textureX = 0
        if (getter()) {
            textureX = 14
        }
        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            textureX += 28
        }
        context.drawTexture(texture, x, y, textureX, 0, this.width, this.height)
        if (getter()) {
            context.drawTexture(
                texture, x, y, this.width, this.height, 0f, 14f, width * 2, height * 2, 256, 256
            )
        } else {
            context.drawTexture(
                texture, x, y, this.width, this.height, 28f, 14f, width * 2, height * 2, 256, 256
            )
        }

        RenderSystem.disableBlend()
        RenderSystem.disableDepthTest()

        if (this.isHovered && (enabledTooltip() != null) && (disabledTooltip() != null)) {
            tooltip = Tooltip.of(
                (if (getter()) enabledTooltip else disabledTooltip)()?.copy()?.formatted(Formatting.WHITE)
            )
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    override fun onPress() = setter(!getter())
}