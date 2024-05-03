package de.huebcraft.mods.enderio.conduits.lang

import de.huebcraft.mods.enderio.build.BuildConstants

object ConduitTranslationKeys {
    const val ITEM_GROUP = "${BuildConstants.modId}.conduits.itemgroup"
    object Gui {
        object Conduit {
            const val INSERT = "gui.${BuildConstants.modId}.conduit.insert"
            const val EXTRACT = "gui.${BuildConstants.modId}.conduit.extract"
            const val CHANNEL = "gui.${BuildConstants.modId}.conduit_channel"
        }

        object Redstone {
            const val MODE = "gui.${BuildConstants.modId}.redstone.mode"
            const val CHANNEL = "gui.${BuildConstants.modId}.redstone_channel"
        }

        object RoundRobin {
            const val ENABLED = "gui.${BuildConstants.modId}.round_robin.enabled"
            const val DISABLED = "gui.${BuildConstants.modId}.round_robin.disabled"
        }

        object SelfFeed {
            const val ENABLED = "gui.${BuildConstants.modId}.self_feed.enabled"
            const val DISABLED = "gui.${BuildConstants.modId}.self_feed.disabled"
        }
    }

    object Redstone {
        const val ALWAYS_ACTIVE = "${BuildConstants.modId}.redstone.always_active"
        const val ACTIVE_WITH_SIGNAL = "${BuildConstants.modId}.redstone.active_with_signal"
        const val ACTIVE_WITHOUT_SIGNAL = "${BuildConstants.modId}.redstone.active_without_signal"
        const val NEVER_ACTIVE = "${BuildConstants.modId}.redstone.never_active"
    }
}