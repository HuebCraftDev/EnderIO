package de.huebcraft.mods.enderio.conduits.conduit

enum class SlotType(val x: Int, val y: Int = 71) {
    FILTER_EXTRACT(113),
    FILTER_INSERT(23),
    UPGRADE_EXTRACT(131);

    fun isAvailableFor(menuData: IConduitMenuData): Boolean {
        return when (this) {
            FILTER_EXTRACT -> menuData.hasFilterExtract
            FILTER_INSERT -> menuData.hasFilterInsert
            UPGRADE_EXTRACT -> menuData.hasUpgrade
        }
    }
}