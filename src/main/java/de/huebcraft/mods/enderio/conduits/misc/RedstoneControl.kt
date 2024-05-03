package de.huebcraft.mods.enderio.conduits.misc

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.lang.ConduitTranslationKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i
import org.joml.Vector2ic

enum class RedstoneControl(val isActive: (Boolean) -> Boolean, override val tooltip: Text): IIcon {
    ALWAYS_ACTIVE({true}, Text.translatable(ConduitTranslationKeys.Redstone.ALWAYS_ACTIVE)),
    ACTIVE_WITH_SIGNAL({it}, Text.translatable(ConduitTranslationKeys.Redstone.ACTIVE_WITH_SIGNAL)),
    ACTIVE_WITHOUT_SIGNAL({!it}, Text.translatable(ConduitTranslationKeys.Redstone.ACTIVE_WITHOUT_SIGNAL)),
    NEVER_ACTIVE({false}, Text.translatable(ConduitTranslationKeys.Redstone.NEVER_ACTIVE));

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