package de.huebcraft.mods.enderio.conduits.client.gui.widget

import com.mojang.blaze3d.systems.RenderSystem
import de.huebcraft.mods.enderio.conduits.client.gui.screen.ConduitScreen
import de.huebcraft.mods.enderio.conduits.client.gui.screen.IEnderScreen
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text
import org.joml.Vector2i


class ConduitSelectionButton(
    i: Int, j: Int, val type: IConduitType<*>, val getter: () -> IConduitType<*>, val setter: (IConduitType<*>) -> Unit
) : PressableWidget(i, j, 21, 24, Text.empty()) {
    override fun onPress() {
        setter(type)
    }

    override fun isValidClickButton(button: Int): Boolean {
        return super.isValidClickButton(button) && getter() != type
    }

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        context.drawTexture(ConduitScreen.TEXTURE, x, y, 227, 0, this.width, this.height)
        if (getter() === type) {
            context.drawTexture(ConduitScreen.TEXTURE, x - 3, y, 224, 0, 3, this.height)
        }
        RenderSystem.disableBlend()
        RenderSystem.disableDepthTest()
        IEnderScreen.renderIcon(context, Vector2i(x, y).add(3, 6), type.clientData)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}
}