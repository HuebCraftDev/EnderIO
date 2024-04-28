package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.build.BuildConstants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.util.ModelIdentifier

@Environment(EnvType.CLIENT)
class ConduitModelLoadingPlugin : ModelLoadingPlugin {
    val CONDUIT_BUNDLE_MODEL = ModelIdentifier(BuildConstants.modId, "conduit_bundle", "")

    override fun onInitializeModelLoader(pluginContext: ModelLoadingPlugin.Context) {
        pluginContext.modifyModelOnLoad().register { orig, context ->
            if (context.id() == CONDUIT_BUNDLE_MODEL) return@register ConduitBlockModel()
            orig
        }
    }
}