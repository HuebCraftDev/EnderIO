package de.huebcraft.mods.enderio.conduits.client.gui.widget

import com.mojang.blaze3d.systems.RenderSystem
import de.huebcraft.mods.enderio.conduits.client.gui.IFullScreenListener
import de.huebcraft.mods.enderio.conduits.client.gui.screen.IEnderScreen
import de.huebcraft.mods.enderio.conduits.misc.IIcon
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW

class EnumIconWidget<T, U>(
    private val screen: U,
    x: Int,
    y: Int,
    private val getter: () -> T,
    private val setter: (T) -> Unit,
    val optionName: Text
) : ClickableWidget(x, y, getter().renderSize.x(), getter().renderSize.y(), Text.empty()),
    IFullScreenListener where T : Enum<T>, T : IIcon, U : Screen, U : IEnderScreen {
    companion object {
        const val ELEMENTS_IN_ROW = 5
        const val SPACE_BETWEEN_ELEMENTS = 4
    }

    private val selection = SelectionScreen()
    private val icons = mutableMapOf<T, SelectionWidget>()

    private val expandTopLeft: Vector2i
    private val expandBottomRight: Vector2i

    var isExpanded = false
        private set
    private var expandNext = false
    private var mouseButton = 0

    init {
        val values = getter().declaringJavaClass.enumConstants
        val firstPos = calcFirstPos(values[0], values.size)
        val elementDistance = values[0].renderSize.add(SPACE_BETWEEN_ELEMENTS, SPACE_BETWEEN_ELEMENTS, Vector2i())
        for ((i, value) in values.withIndex()) {
            val subWidgetPos = firstPos.add(
                (i % ELEMENTS_IN_ROW) * elementDistance.x, i / ELEMENTS_IN_ROW * elementDistance.y, Vector2i()
            ).add(x, y)
            val widget = SelectionWidget(subWidgetPos, value)
            icons[value] = widget
        }

        val topLeft = Vector2i(Int.MAX_VALUE, Int.MAX_VALUE)
        val bottomRight = Vector2i(Int.MIN_VALUE, Int.MIN_VALUE)
        for (widget in icons.values) {
            topLeft.x = topLeft.x.coerceAtMost(widget.x)
            topLeft.y = topLeft.y.coerceAtMost(widget.y)
            bottomRight.x = bottomRight.x.coerceAtLeast(widget.x + widget.width)
            bottomRight.y = bottomRight.y.coerceAtLeast(widget.y + widget.height)
        }
        expandTopLeft = topLeft.add(-SPACE_BETWEEN_ELEMENTS, -SPACE_BETWEEN_ELEMENTS)
        expandBottomRight = bottomRight.add(SPACE_BETWEEN_ELEMENTS, SPACE_BETWEEN_ELEMENTS)
    }

    private fun calcFirstPos(icon: T, amount: Int): Vector2i {
        val width = (amount.coerceAtMost(ELEMENTS_IN_ROW) - 1) * (icon.renderSize.x() + SPACE_BETWEEN_ELEMENTS)
        return Vector2i(-width / 2, 2 * SPACE_BETWEEN_ELEMENTS + icon.renderSize.y())
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        mouseButton = button
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (isExpanded) {
            selectNext(mouseButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        } else {
            isExpanded = true
            MinecraftClient.getInstance().setScreen(selection)
        }
    }

    private fun selectNext(isForward: Boolean) {
        val values = getter().declaringJavaClass.enumConstants
        val index = getter().ordinal + (if (isForward) 1 else -1) + values.size
        setter(values[index % values.size])
    }

    override fun isValidClickButton(button: Int): Boolean {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
    }

    private var tooltipDisplayCache: T? = null

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (expandNext && MinecraftClient.getInstance().currentScreen === screen) {
            MinecraftClient.getInstance().setScreen(selection)
            expandNext = false
            isExpanded = true
        }
        val icon = getter()
        screen.renderIconBackground(context, Vector2i(x, y), icon)
        IEnderScreen.renderIcon(context, Vector2i(x, y), icon)

        if (isHovered && tooltipDisplayCache !== getter()) {
            tooltipDisplayCache = getter()
            tooltip =
                Tooltip.of(optionName.copy().append("\n").append(getter().tooltip.copy().formatted(Formatting.GRAY)))
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    override fun onGlobalClick(mouseX: Double, mouseY: Double) {
        if (isExpanded && !(expandTopLeft.x <= mouseX && expandBottomRight.x >= mouseX && expandTopLeft.y <= mouseY && expandBottomRight.y >= mouseY || isMouseOver(
                mouseX, mouseY
            ))
        ) {
            isExpanded = false
            MinecraftClient.getInstance().setScreen(screen)
        }
    }

    fun setExpanded(expanded: Boolean) {
        expandNext = expanded
        isExpanded = expanded
    }

    inner class SelectionScreen : Screen(Text.empty()), IEnderScreen {
        override fun init() {
            addSelectableChild(this@EnumIconWidget)
            this@EnumIconWidget.icons.values.forEach(::addDrawableChild)
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            RenderSystem.disableDepthTest()
            screen.render(context, mouseX, mouseY, delta)
            renderSimpleArea(context, expandTopLeft, expandBottomRight)
            super.render(context, mouseX, mouseY, delta)
            RenderSystem.enableDepthTest()
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            for (widget in children()) {
                if (widget is ClickableWidget && widget.isNarratable && widget is IFullScreenListener) widget.onGlobalClick(
                    mouseX, mouseY
                )
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }

        public override fun setTooltip(tooltip: Text?) {
            super.setTooltip(tooltip)
        }

        override fun shouldPause(): Boolean = false

        override fun renderBackground(context: DrawContext) {
        }

        override fun close() {
            this@EnumIconWidget.isFocused = false
            this@EnumIconWidget.isExpanded = false
            super.close()
        }

        override fun resize(client: MinecraftClient, width: Int, height: Int) {
            client.setScreen(screen)
        }
    }

    inner class SelectionWidget(pos: Vector2i, val value: T) :
        ClickableWidget(pos.x, pos.y, value.renderSize.x() + 2, value.renderSize.y() + 2, value.tooltip) {

        override fun onClick(mouseX: Double, mouseY: Double) {
            super.onClick(mouseX, mouseY)
            setter(value)
        }

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            if (getter() != value) {
                selection.renderIconBackground(context, Vector2i(x, y), value)
            } else {
                context.fill(x, y, x + width - 2, y + height - 2, -0xffdf01)
                context.fill(x + 1, y + 1, x + width - 3, y + height - 3, -0x747475)
            }
            IEnderScreen.renderIcon(context, Vector2i(x, y), value)

            if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
                val tooltip = value.tooltip
                if (tooltip != Text.empty()) selection.setTooltip(tooltip)
            }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        }
    }
}