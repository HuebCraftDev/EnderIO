package de.huebcraft.mods.enderio.api.integration

import de.huebcraft.mods.enderio.conduits.integration.ae2.AE2Integration

object IntegrationManager {
    val ALL_INTEGRATIONS = mutableListOf<EnderIOPlugin>()

    val AE2_INTEGRATION = IntegrationWrapper("ae2", ::AE2Integration)
}