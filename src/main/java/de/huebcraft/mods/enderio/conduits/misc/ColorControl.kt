package de.huebcraft.mods.enderio.conduits.misc

import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i

enum class ColorControl(val color: Int, val colorActive: Int) : IIcon {
    GREEN(-0xc4aee6, -0xb99ee1),
    BROWN(-0xaecfe6, -0x9ec6e1),
    BLUE(-0xdace6e, -0xd3c551),
    PURPLE(-0x84d042, -0x6cc71c),
    CYAN(-0xd78969, -0xcf724b),
    LIGHT_GRAY(-0x777778, -0x545455),
    GRAY(-0xbcbcbd, -0xafafb0),
    PINK(-0x539887, -0x277e68),
    LIME(-0xbe32cc, -0xb109c2),
    YELLOW(-0x4e5adf, -0x2130d6),
    LIGHT_BLUE(-0xae9258, -0x99762d),
    MAGENTA(-0x63bc5c, -0x3cab33),
    ORANGE(-0x4393ca, -0x1477bc),
    WHITE(-0x3f3f40, -0xf0f10),
    BLACK(-0xe1e4e5, -0xdbdfe0),
    RED(-0x4cced4, -0x29c5cc);

    companion object {
        private val TEXTURE = Identifier("enderio", "textures/gui/icons/color_control.png")
        private val SIZE = Vector2i(16, 16)
    }

    private val pos: Vector2i = Vector2i(16 * ordinal, 0)

    override val textureLocation: Identifier
        get() = TEXTURE

    override val tooltip: Text
        get() = Text.literal(name.lowercase().replace('_', ' '))

    override val iconSize: Vector2i
        get() = SIZE

    override val texturePosition: Vector2i
        get() = pos

    override val textureSize: Vector2i
        get() = Vector2i(256, 16)
}
