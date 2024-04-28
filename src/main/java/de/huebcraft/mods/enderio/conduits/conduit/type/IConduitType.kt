package de.huebcraft.mods.enderio.conduits.conduit.type

import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.ticker.IConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.RedstoneControl
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

interface IConduitType<T : IExtendedConduitData<T>> {
    fun canBeInSameBlock(other: IConduitType<*>): Boolean = true

    fun canBeReplacedBy(other: IConduitType<*>): Boolean = false

    fun getTexture(extendedConduitData: T): Identifier

    val itemTexture: Identifier

    fun getConduitItem(): Item = Registries.ITEM.get(ConduitTypes.REGISTRY.getId(this))

    val ticker: IConduitTicker

    val clientData: IClientConduitData<T>

    val menuData: IConduitMenuData

    fun createExtendedConduitData(world: World, pos: BlockPos): T

    fun <K> proxyLookup(
        lookup: BlockApiLookup<K, Direction?>,
        extendedConduitData: T,
        world: World,
        pos: BlockPos,
        direction: Direction?,
        state: InWorldNode.IOState?
    ): K? = null

    fun getDefaultConnection(world: World, pos: BlockPos, direction: Direction): ConduitConnectionData =
        ConduitConnectionData(isInsert = false, isExtract = true, control = RedstoneControl.NEVER_ACTIVE)

    data class ConduitConnectionData(val isInsert: Boolean, val isExtract: Boolean, val control: RedstoneControl)
}