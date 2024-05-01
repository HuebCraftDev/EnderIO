package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.build.BuildConstants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class ConduitModelLoadingPlugin : ModelLoadingPlugin {
    val CONDUIT_BUNDLE_MODEL = Identifier(BuildConstants.modId, "conduit")

    override fun onInitializeModelLoader(pluginContext: ModelLoadingPlugin.Context) {
        pluginContext.addModels(ConduitBlockModel.CONDUIT_ALL)
        pluginContext.modifyModelOnLoad().register { orig, context ->
            if (context.id().namespace == CONDUIT_BUNDLE_MODEL.namespace && context.id().path.substringBefore('#') == CONDUIT_BUNDLE_MODEL.path) {
                return@register ConduitBlockModel()
            }
            orig
        }
    }
}