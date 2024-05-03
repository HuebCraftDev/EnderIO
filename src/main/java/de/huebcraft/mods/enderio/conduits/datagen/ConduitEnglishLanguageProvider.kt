package de.huebcraft.mods.enderio.conduits.datagen

import de.huebcraft.mods.enderio.conduits.lang.ConduitTranslationKeys
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider

class ConduitEnglishLanguageProvider(dataOutput: FabricDataOutput) :
    FabricLanguageProvider(dataOutput, "en_us") {

    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        translationBuilder.add(ConduitTranslationKeys.ITEM_GROUP, "EnderIO - Conduits")

        translationBuilder.add(ConduitTranslationKeys.Gui.Conduit.INSERT, "Insert")
        translationBuilder.add(ConduitTranslationKeys.Gui.Conduit.EXTRACT, "Extract")
        translationBuilder.add(ConduitTranslationKeys.Gui.Conduit.CHANNEL, "Conduit-Channel")

        translationBuilder.add(ConduitTranslationKeys.Gui.Redstone.MODE, "Redstone Mode")
        translationBuilder.add(ConduitTranslationKeys.Gui.Redstone.CHANNEL, "Redstone-Channel")

        translationBuilder.add(ConduitTranslationKeys.Gui.RoundRobin.ENABLED, "Round Robin Enabled")
        translationBuilder.add(ConduitTranslationKeys.Gui.RoundRobin.DISABLED, "Round Robin Disabled")

        translationBuilder.add(ConduitTranslationKeys.Gui.SelfFeed.ENABLED, "Self Feed Enabled")
        translationBuilder.add(ConduitTranslationKeys.Gui.SelfFeed.DISABLED, "Self Feed Disabled")

        translationBuilder.add(ConduitTranslationKeys.Redstone.ALWAYS_ACTIVE, "Always Active")
        translationBuilder.add(ConduitTranslationKeys.Redstone.ACTIVE_WITH_SIGNAL, "Active with Signal")
        translationBuilder.add(ConduitTranslationKeys.Redstone.ACTIVE_WITHOUT_SIGNAL, "Active without Signal")
        translationBuilder.add(ConduitTranslationKeys.Redstone.NEVER_ACTIVE, "Never Active")
    }
}