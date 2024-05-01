package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.energy.EnergyConduitType
import de.huebcraft.mods.enderio.conduits.enderconduits.redstone.RedstoneConduitType
import net.minecraft.util.Identifier

object EnderConduitTypes : Registrar<IConduitType<*>>(ConduitTypes.REGISTRY) {
    val ICON_TEXTURE = Identifier(BuildConstants.modId, "textures/gui/conduit_icon.png")

    val REDSTONE = register("redstone_conduit", ::RedstoneConduitType)
    val ENERGY = register("energy_conduit", ::EnergyConduitType)
}