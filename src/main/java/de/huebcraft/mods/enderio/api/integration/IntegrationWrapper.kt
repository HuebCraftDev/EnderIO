package de.huebcraft.mods.enderio.api.integration

import net.fabricmc.loader.api.FabricLoader

class IntegrationWrapper<T : EnderIOPlugin>(val modId: String, supplier: () -> T) {
    val value: T? = if (FabricLoader.getInstance().isModLoaded(modId)) supplier()
    else null

    init {
        if (value != null) IntegrationManager.ALL_INTEGRATIONS.add(value)
    }
}