package de.huebcraft.mods.enderio.api.integration

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder

interface EnderIOPlugin {
    fun onEnderIOInitialized() = Unit

    fun onEnglishTranslationsAdded(translationBuilder: TranslationBuilder) = Unit
}