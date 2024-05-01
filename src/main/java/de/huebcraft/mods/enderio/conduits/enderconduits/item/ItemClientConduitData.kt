package de.huebcraft.mods.enderio.conduits.enderconduits.item

import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import net.minecraft.util.Identifier
import org.joml.Vector2i
import org.joml.Vector2ic

class ItemClientConduitData : IClientConduitData<ItemExtendedData> {


    override val textureLocation: Identifier = EnderConduitTypes.ICON_TEXTURE
    override val texturePosition: Vector2ic = Vector2i(0, 96)
}