package de.huebcraft.mods.enderio.conduits.client.gui.screen

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.client.gui.IFullScreenListener
import de.huebcraft.mods.enderio.conduits.client.gui.widget.CheckBox
import de.huebcraft.mods.enderio.conduits.client.gui.widget.ConduitSelectionButton
import de.huebcraft.mods.enderio.conduits.client.gui.widget.EnumIconWidget
import de.huebcraft.mods.enderio.conduits.conduit.ConduitBundle
import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.SlotType
import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.lang.ConduitTranslationKeys
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import de.huebcraft.mods.enderio.conduits.mixin.ScreenAccessor
import de.huebcraft.mods.enderio.conduits.network.C2SSetConduitConnectionState
import de.huebcraft.mods.enderio.conduits.network.C2SSetConduitExtendedData
import de.huebcraft.mods.enderio.conduits.screen.ConduitScreenHandler
import de.huebcraft.mods.enderio.conduits.screen.ConduitSlot
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i


@Environment(EnvType.CLIENT)
class ConduitScreen(
    handler: ConduitScreenHandler, inventory: PlayerInventory, title: Text
) : HandledScreen<ConduitScreenHandler>(handler, inventory, title), IEnderScreen {
    companion object {
        val TEXTURE = Identifier(BuildConstants.modId, "textures/gui/conduit.png")
        val TEXTURE_SIZE = Vector2i(206, 195)
    }

    private val typedButtons = mutableListOf<ClickableWidget>()
    private val typeSelectionButtons = mutableListOf<ConduitSelectionButton>()
    private var recalculateTypedButtons = true

    override fun init() {
        super.init()
        updateConnectionWidgets(true)
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val oldEnums = mutableMapOf<String, Boolean>()
        for (drawable in (this as ScreenAccessor).drawables) {
            if (drawable is EnumIconWidget<*, *>) oldEnums[drawable.optionName.string] = drawable.isExpanded
        }
        super.resize(client, width, height)
        for (drawable in (this as ScreenAccessor).drawables) {
            if (drawable is EnumIconWidget<*, *>) drawable.setExpanded(
                oldEnums.getOrDefault(
                    drawable.optionName.string, false
                )
            )
        }
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in children()) {
            element as? ClickableWidget ?: continue
            if (element.isNarratable && element is IFullScreenListener) element.onGlobalClick(mouseX, mouseY)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        val focusedWidget = focused as? ClickableWidget
        if (focusedWidget != null && focusedWidget.isNarratable) return focusedWidget.mouseDragged(
            mouseX, mouseY, button, deltaX, deltaY
        )
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(TEXTURE, x, y, 0, 0, TEXTURE_SIZE.x, TEXTURE_SIZE.y)

        val menuData = handler.conduitType.menuData
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        if (menuData.showBarSeparator) {
            context.drawTexture(TEXTURE, 102, 7, 255, 0, 1, 97)
        }
        for (type in SlotType.entries) {
            if (type.isAvailableFor(menuData)) {
                context.drawTexture(TEXTURE, type.x - 1, type.y - 1, 206, 0, 18, 18)
            }
        }
        context.matrices.pop()
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        val menuData = handler.conduitType.menuData
        context.drawText(textRenderer, Text.translatable(ConduitTranslationKeys.Gui.Conduit.INSERT), 22 + 16, 7 + 4, 4210752, false)
        if (menuData.showBothEnable) {
            context.drawText(
                textRenderer, Text.translatable(ConduitTranslationKeys.Gui.Conduit.EXTRACT), 112 + 16, 7 + 4, 4210752, false
            )
        }
    }

    private fun updateConnectionWidgets(forceUpdate: Boolean) {
        if (forceUpdate || recalculateTypedButtons) {
            recalculateTypedButtons = false
            typedButtons.forEach(this::remove)
            typedButtons.clear()
            val data = handler.conduitType.menuData
            val pos = Vector2i(22, 7).add(x, y)
            addTypedButton(
                CheckBox(pos,
                    { getDynStateOrDefault({ it.isInsert }, false) },
                    { bool -> changeDynState { it.withEnabled(false, bool) } })
            )

            if (data.showBothEnable) {
                addTypedButton(
                    CheckBox(pos.add(90, 0, Vector2i()),
                        { getDynStateOrDefault({ it.isExtract }, false) },
                        { bool -> changeDynState { it.withEnabled(true, bool) } })
                )
            }
            if (data.showColorInsert) {
                addTypedButton(
                    EnumIconWidget(this,
                        pos.x(),
                        pos.y() + 20,
                        {
                            getDynStateOrDefault({ it.insert }, ColorControl.GREEN)
                        },
                        { color -> changeDynState { it.withColor(false, color) } },
                        Text.translatable(ConduitTranslationKeys.Gui.Conduit.CHANNEL)
                    )
                )
            }
            if (data.showColorExtract) {
                addTypedButton(
                    EnumIconWidget(this,
                        pos.x() + 90,
                        pos.y() + 20,
                        {
                            getDynStateOrDefault({ it.extract }, ColorControl.GREEN)
                        },
                        { color -> changeDynState { it.withColor(true, color) } },
                        Text.translatable(ConduitTranslationKeys.Gui.Conduit.CHANNEL)
                    )
                )
            }
            if (data.showRedstoneExtract) {
                addTypedButton(
                    EnumIconWidget(this,
                        pos.x() + 90,
                        pos.y() + 40,
                        {
                            getDynStateOrDefault(
                                { it.redstoneControl }, RedstoneControl.ACTIVE_WITH_SIGNAL
                            )
                        },
                        { mode -> changeDynState { it.withRedstoneMode(mode) } },
                        Text.translatable(ConduitTranslationKeys.Gui.Redstone.MODE)
                    )
                )
                addTypedButton(
                    EnumIconWidget(this,
                        pos.x() + 90 + 20,
                        pos.y() + 40,
                        {
                            getDynStateOrDefault(
                                { it.redstoneChannel }, ColorControl.GREEN
                            )
                        },
                        { color -> changeDynState { it.withRedstoneChannel(color) } },
                        Text.translatable(ConduitTranslationKeys.Gui.Redstone.CHANNEL)
                    )
                )
            }
            createWidgetsUnsafe(handler.conduitType.clientData)
        }
        val validConnections = mutableListOf<IConduitType<*>>()
        for (type in getBundle().types) {
            if (getConnectionState(type) is IConnectionState.DynamicConnectionState) {
                validConnections.add(type)
            }
        }
        if (forceUpdate || typeSelectionButtons.map(ConduitSelectionButton::getType) != validConnections) {
            typeSelectionButtons.forEach(this::remove)
            typeSelectionButtons.clear()
            for (i in validConnections.indices) {
                val connection = validConnections[i]
                val button = ConduitSelectionButton(
                    x + 206, y + 4 + 24 * i, connection, handler::conduitType
                ) { type ->
                    handler.conduitType = type
                    recalculateTypedButtons = true
                }

                typeSelectionButtons.add(button)
                addDrawableChild(button)
            }
        }
    }

    private fun <T : IExtendedConduitData<T>> createWidgetsUnsafe(clientData: IClientConduitData<T>) {
        @Suppress("UNCHECKED_CAST")
        clientData.createWidgets(
            this,
            getBundle().getNodeFor(handler.conduitType).extendedConduitData as T,
            { mapper -> sendExtendedConduitUpdate { mapper(it as T) } },
            handler::direction,
            Vector2i(22, 7).add(x, y)
        ).forEach { button -> this.addTypedButton(button) }
    }

    private fun sendExtendedConduitUpdate(map: (IExtendedConduitData<*>) -> IExtendedConduitData<*>) {
        val currentData = getBundle().getNodeFor(handler.conduitType).extendedConduitData
        ClientPlayNetworking.send(
            C2SSetConduitExtendedData(
                handler.blockEntity!!.pos, handler.conduitType, map(currentData).serializeGuiNbt()
            )
        )
    }

    private fun addTypedButton(widget: ClickableWidget) {
        typedButtons.add(widget)
        addDrawableChild(widget)
    }

    private fun changeDynState(map: (IConnectionState.DynamicConnectionState) -> IConnectionState.DynamicConnectionState) {
        val dynState = getConnectionState() as? IConnectionState.DynamicConnectionState ?: return
        ClientPlayNetworking.send(
            C2SSetConduitConnectionState(
                handler.blockEntity!!.pos, handler.direction, handler.conduitType, map(dynState)
            )
        )
    }

    private fun <T> getDynStateOrDefault(map: (IConnectionState.DynamicConnectionState) -> T, default: T): T {
        val dynState = getConnectionState() as? IConnectionState.DynamicConnectionState ?: return default
        return map(dynState)
    }

    private fun getConnectionState(): IConnectionState = getConnectionState(handler.conduitType)

    private fun getConnectionState(type: IConduitType<*>): IConnectionState =
        getBundle().getConnection(handler.direction).getConnectionState(type)

    private fun getBundle(): ConduitBundle = handler.blockEntity!!.bundle

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!handler.canUse(client!!.player!!)) {
            client?.player?.closeScreen()
        } else {
            updateConnectionWidgets(false)
            handler.conduitSlots.forEach(ConduitSlot::updateVisibilityPosition)
            if (handler.blockEntity != null) {
                super.render(context, mouseX, mouseY, delta)
            }
        }
    }
}