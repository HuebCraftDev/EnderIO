package de.huebcraft.mods.enderio.conduits.client

import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.client.gui.screen.ConduitScreen
import de.huebcraft.mods.enderio.conduits.client.model.ConduitModelLoadingPlugin
import de.huebcraft.mods.enderio.conduits.init.ConduitBlocks
import de.huebcraft.mods.enderio.conduits.init.ConduitScreenHandlers
import de.huebcraft.mods.enderio.conduits.network.ConduitNetworking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.RenderLayer
import org.apache.logging.log4j.LogManager

@Environment(EnvType.CLIENT)
internal object ClientMain : ClientModInitializer {
    internal val LOGGER = LogManager.getLogger(BuildConstants.modName)

    override fun onInitializeClient() {
        ModelLoadingPlugin.register(ConduitModelLoadingPlugin())
        LOGGER.info("ClientMain has been initialized")
        // TODO Conduit tint for facade
        // TODO Translations
        HandledScreens.register(ConduitScreenHandlers.CONDUIT_SCREEN_HANDLER(), ::ConduitScreen)
        BlockRenderLayerMap.INSTANCE.putBlock(ConduitBlocks.CONDUIT(), RenderLayer.getCutout())
        ConduitNetworking.registerClientReceiver()
    }
}