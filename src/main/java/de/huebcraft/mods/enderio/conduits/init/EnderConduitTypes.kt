package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.conduit.type.SimpleConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.energy.EnergyConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.fluid.FluidConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.item.ItemClientConduitData
import de.huebcraft.mods.enderio.conduits.enderconduits.item.ItemConduitTicker
import de.huebcraft.mods.enderio.conduits.enderconduits.item.ItemExtendedData
import de.huebcraft.mods.enderio.conduits.enderconduits.redstone.RedstoneConduitType
import net.minecraft.util.Identifier
import org.joml.Vector2i

data object EnderConduitTypes : Registrar<IConduitType<*>>(ConduitTypes.REGISTRY) {
    val ICON_TEXTURE = Identifier(BuildConstants.modId, "textures/gui/conduit_icon.png")

    val REDSTONE = register("redstone_conduit", ::RedstoneConduitType)
    val ENERGY = register("energy_conduit", ::EnergyConduitType)
    val ITEM = register("item_conduit") {
        SimpleConduitType(
            Identifier(BuildConstants.modId, "block/conduit/item"),
            ItemConduitTicker(),
            ::ItemExtendedData,
            ItemClientConduitData(),
            IConduitMenuData.ITEM
        )
    }
    val FLUID1 = registerFluidConduit("fluid_conduit", 50, false, Vector2i(0, 120))
    val FLUID2 = registerFluidConduit("pressurized_fluid_conduit", 100, false, Vector2i(0, 144))
    val FLUID3 = registerFluidConduit("ender_fluid_conduit", 200, true, Vector2i(0, 168))

    private fun registerFluidConduit(name: String, tier: Int, isMultiFluid: Boolean, iconPos: Vector2i) = register(name) {
        FluidConduitType(Identifier(BuildConstants.modId, "block/conduit/$name"), tier, isMultiFluid, ICON_TEXTURE, iconPos)
    }
}