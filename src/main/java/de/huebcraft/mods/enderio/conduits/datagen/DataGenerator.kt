package de.huebcraft.mods.enderio.conduits.datagen

import de.huebcraft.mods.enderio.build.BuildConstants
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import org.apache.logging.log4j.LogManager

internal object DataGenerator : DataGeneratorEntrypoint {
    private val LOGGER = LogManager.getLogger(BuildConstants.modName)

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        LOGGER.info("Starting Data Generation")
        // TODO recipes
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::ConduitTagProvider)
        pack.addProvider(::ConduitModelProvider)
    }
}