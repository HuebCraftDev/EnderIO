package de.huebcraft.mods.enderio.conduits.conduit.type

import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.util.Identifier
import org.joml.Vector2i

abstract class TieredConduit<T : IExtendedConduitData<T>>(
    val texture: Identifier, val type: Identifier, val tier: Int, iconTexture: Identifier, iconPos: Vector2i
) : IConduitType<T> {
    @Environment(EnvType.CLIENT)
    override val clientData: IClientConduitData<T> = IClientConduitData.Simple(iconTexture, iconPos)

    override val itemTexture: Identifier = texture

    override fun getTexture(extendedConduitData: T) = texture

    override fun canBeInSameBlock(other: IConduitType<*>): Boolean {
        if (other !is TieredConduit<*>) return true
        return type != other.type
    }

    override fun canBeReplacedBy(other: IConduitType<*>): Boolean {
        if (other !is TieredConduit<*>) return false
        if (type == other.type) return tier < other.tier
        return false
    }
}