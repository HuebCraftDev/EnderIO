package de.huebcraft.mods.enderio.conduits.init

import de.huebcraft.mods.enderio.conduits.screen.ConduitScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandlerType

data object ConduitScreenHandlers : Registrar<ScreenHandlerType<*>>(Registries.SCREEN_HANDLER) {
    val CONDUIT_SCREEN_HANDLER = register("conduit") {
        ExtendedScreenHandlerType(ConduitScreenHandler::factory)
    }
}