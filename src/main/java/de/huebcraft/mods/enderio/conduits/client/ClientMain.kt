package de.huebcraft.mods.enderio.conduits.client

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.client.model.ConduitModelLoadingPlugin
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import org.apache.logging.log4j.LogManager

@Environment(EnvType.CLIENT)
internal object ClientMain : ClientModInitializer {
    internal val LOGGER = LogManager.getLogger(BuildConstants.modName)

    override fun onInitializeClient() {
        ModelLoadingPlugin.register(ConduitModelLoadingPlugin())
        LOGGER.info("ClientMain has been initialized")
    }
}