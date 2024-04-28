package de.huebcraft.mods.enderio.conduits.conduit

import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes

sealed interface RightClickAction {
    data class Upgrade(val notInConduit: IConduitType<*>) : RightClickAction {
        override fun toString(): String = "Upgrade[${ConduitTypes.REGISTRY.getId(notInConduit)}]"
    }

    object Insert : RightClickAction {
        override fun toString(): String = "Insert"
    }

    object Blocked : RightClickAction {
        override fun toString(): String = "Blocked"
    }

    fun hasChanged(): Boolean = this !is Blocked
}