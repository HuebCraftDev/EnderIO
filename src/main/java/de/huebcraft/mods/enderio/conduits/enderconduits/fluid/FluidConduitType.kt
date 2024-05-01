package de.huebcraft.mods.enderio.conduits.enderconduits.fluid

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.ticker.IConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.TieredConduit
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.joml.Vector2i

class FluidConduitType(
    texture: Identifier, private val transferRate: Int, private val isMultiFluid: Boolean, iconTexture: Identifier, iconPos: Vector2i
) : TieredConduit<FluidExtendedData>(
    texture, Identifier(BuildConstants.modId, "fluid"), if (isMultiFluid) 100_000 else 0 + transferRate, iconTexture, iconPos
) {
    companion object {
        val MENU_DATA = IConduitMenuData.Simple(
            hasFilterInsert = false,
            hasFilterExtract = false,
            hasUpgrade = false,
            showColorInsert = false,
            showColorExtract = false,
            showRedstoneExtract = true
        )
    }

    override val ticker: IConduitTicker = FluidConduitTicker(!isMultiFluid, transferRate)
    override val menuData: IConduitMenuData = MENU_DATA

    override fun createExtendedConduitData(world: World, pos: BlockPos): FluidExtendedData = FluidExtendedData(isMultiFluid)
}