package de.huebcraft.mods.enderio.conduits.enderconduits.item

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.client.gui.widget.CheckBox
import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import de.huebcraft.mods.enderio.conduits.lang.ConduitTranslationKeys
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import org.joml.Vector2i
import org.joml.Vector2ic

class ItemClientConduitData : IClientConduitData<ItemExtendedData> {

    override fun createWidgets(
        screen: Screen,
        extendedData: ItemExtendedData,
        updateData: ((ItemExtendedData) -> ItemExtendedData) -> Unit,
        direction: () -> Direction,
        widgetsStart: Vector2ic
    ): List<ClickableWidget> {
        val widgets = mutableListOf<ClickableWidget>()
        widgets.add(
            CheckBox(
                Identifier(BuildConstants.modId, "textures/gui/round_robin.png"),
                widgetsStart.add(110, 20, Vector2i()),
                { extendedData.get(direction()).roundRobin },
                { bool ->
                    updateData { data ->
                        data.compute(direction()).roundRobin = bool
                        data
                    }
                },
                { Text.translatable(ConduitTranslationKeys.Gui.RoundRobin.ENABLED) },
                { Text.translatable(ConduitTranslationKeys.Gui.RoundRobin.DISABLED) })
        )
        widgets.add(
            CheckBox(
                Identifier(BuildConstants.modId, "textures/gui/self_feed.png"),
                widgetsStart.add(130, 20, Vector2i()),
                { extendedData.get(direction()).selfFeed },
                { bool ->
                    updateData { data ->
                        data.compute(direction()).selfFeed = bool
                        data
                    }
                },
                { Text.translatable(ConduitTranslationKeys.Gui.SelfFeed.ENABLED) },
                { Text.translatable(ConduitTranslationKeys.Gui.SelfFeed.DISABLED) })
        )
        return widgets
    }

    override val textureLocation: Identifier = EnderConduitTypes.ICON_TEXTURE
    override val texturePosition: Vector2ic = Vector2i(0, 96)
}