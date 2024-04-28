package de.huebcraft.mods.enderio.conduits.conduit.type

import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.util.Identifier

abstract class TieredConduit<T : IExtendedConduitData<T>>(val texture: Identifier, val type: Identifier, val tier: Int) : IConduitType<T> {
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