package de.huebcraft.mods.enderio.conduits.misc

import de.huebcraft.mods.enderio.build.BuildConstants
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i
import org.joml.Vector2ic

enum class RedstoneControl(val isActive: (Boolean) -> Boolean, override val tooltip: Text): IIcon {
    ALWAYS_ACTIVE({true}, Text.translatable("enderio_conduits.redstone.always_active")),
    ACTIVE_WITH_SIGNAL({it}, Text.translatable("enderio_conduits.redstone.active_with_signal")),
    ACTIVE_WITHOUT_SIGNAL({!it}, Text.translatable("enderio_conduits.redstone.active_without_signal")),
    NEVER_ACTIVE({false}, Text.translatable("enderio_conduits.redstone.never_active"));

    private val pos = Vector2i(16 * ordinal, 0)

    companion object {
        private val TEXTURE = Identifier(BuildConstants.modId, "textures/gui/icons/redstone_control.png")
        private val SIZE = Vector2i(16, 16)
    }

    override val textureLocation: Identifier
        get() = TEXTURE

    override val iconSize: Vector2ic
        get() = SIZE

    override val texturePosition: Vector2ic = pos

    override val textureSize: Vector2ic = Vector2i(64, 16)
}