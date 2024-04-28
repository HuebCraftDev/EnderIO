package de.huebcraft.mods.enderio.conduits.enderconduits.redstone

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.type.SimpleConduitType
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import net.minecraft.util.Identifier
import org.joml.Vector2i

class RedstoneConduitType : SimpleConduitType<RedstoneExtendedData>(INACTIVE, RedstoneConduitTicker(), ::RedstoneExtendedData, EnderConduitTypes.ICON_TEXTURE, Vector2i(0, 0), IConduitMenuData.REDSTONE) {
    companion object {
        val ACTIVE = Identifier(BuildConstants.modId, "block/conduit/redstone_active")
        val INACTIVE = Identifier(BuildConstants.modId, "block/conduit/redstone")
    }

    override fun getTexture(extendedConduitData: RedstoneExtendedData): Identifier {
        return if (extendedConduitData.isActive) ACTIVE else INACTIVE
    }
}