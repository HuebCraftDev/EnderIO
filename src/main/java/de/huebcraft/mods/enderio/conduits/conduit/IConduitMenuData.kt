package de.huebcraft.mods.enderio.conduits.conduit

interface IConduitMenuData {

    companion object {
        val REDSTONE = Simple(
            hasFilterInsert = true, hasFilterExtract = true,
            hasUpgrade = false, showColorInsert = true, showColorExtract = true, showRedstoneExtract = false)
        val ITEM = Simple(
            hasFilterInsert = true,
            hasFilterExtract = true,
            hasUpgrade = true,
            showColorInsert = true,
            showColorExtract = true,
            showRedstoneExtract = true
        )
        val ENERGY = Simple(
            hasFilterInsert = false,
            hasFilterExtract = false,
            hasUpgrade = false,
            showColorInsert = false,
            showColorExtract = false,
            showRedstoneExtract = true
        )
    }

    val hasFilterInsert: Boolean

    val hasFilterExtract: Boolean

    val hasUpgrade: Boolean

    val showBarSeparator: Boolean
        get() = true

    val showBothEnable: Boolean
        get() = true

    val showColorInsert: Boolean

    val showColorExtract: Boolean

    val showRedstoneExtract: Boolean

    data class Simple(
        override val hasFilterInsert: Boolean,
        override val hasFilterExtract: Boolean,
        override val hasUpgrade: Boolean,
        override val showColorInsert: Boolean,
        override val showColorExtract: Boolean,
        override val showRedstoneExtract: Boolean
    ) : IConduitMenuData
}