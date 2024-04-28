package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier

object ConduitTypes {
    val CONDUIT_TYPES: RegistryKey<Registry<IConduitType<*>>> = RegistryKey.ofRegistry(Identifier(BuildConstants.modId, "conduit_types"))
    val REGISTRY: SimpleRegistry<IConduitType<*>> =
        FabricRegistryBuilder.createSimple(CONDUIT_TYPES)
            .attribute(RegistryAttribute.SYNCED).buildAndRegister()
}