package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.conduits.enderconduits.energy.EnergyConduitType
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType

object ModConduitTypes : Registrar<IConduitType<*>>(ConduitTypes.REGISTRY) {
    val ENERGY = register("energy_conduit", ::EnergyConduitType)
}