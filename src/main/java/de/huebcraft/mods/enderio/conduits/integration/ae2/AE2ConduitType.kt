package de.huebcraft.mods.enderio.conduits.integration.ae2

import appeng.api.networking.GridHelper
import appeng.api.networking.IInWorldGridNodeHost
import de.huebcraft.mods.enderio.build.BuildConstants
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.ticker.IConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.conduit.type.TieredConduit
import de.huebcraft.mods.enderio.conduits.init.EnderConduitTypes
import de.huebcraft.mods.enderio.conduits.math.InWorldNode
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.item.Item
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.joml.Vector2i

class AE2ConduitType(val dense: Boolean) : TieredConduit<AE2InWorldConduitNodeHost>(
    Identifier(
        BuildConstants.modId, "block/conduit/${if (dense) "dense_me" else "me"}"
    ),
    Identifier("ae2", "me_cable"),
    if (dense) 32 else 8,
    EnderConduitTypes.ICON_TEXTURE,
    Vector2i(0, if (dense) 72 else 48)
) {
    override val ticker: IConduitTicker = object : IConduitTicker {
        override fun tickGraph(
            type: IConduitType<*>,
            graph: Graph<Mergeable.Dummy>,
            world: ServerWorld,
            isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
        ) {
        }

        override fun canConnectTo(world: World, pos: BlockPos, direction: Direction): Boolean =
            GridHelper.getExposedNode(world, pos.offset(direction), direction.opposite) != null

        override fun hasConnectionDelay(): Boolean = true

        override fun canConnectTo(thisType: IConduitType<*>, other: IConduitType<*>): Boolean = other is AE2ConduitType
    }
    override val menuData: IConduitMenuData = object : IConduitMenuData {
        override val hasFilterInsert: Boolean = false
        override val hasFilterExtract: Boolean = false
        override val hasUpgrade: Boolean = false
        override val showColorInsert: Boolean = false
        override val showColorExtract: Boolean = false
        override val showRedstoneExtract: Boolean = false
    }

    override fun createExtendedConduitData(world: World, pos: BlockPos): AE2InWorldConduitNodeHost = AE2InWorldConduitNodeHost(this)

    override fun <A, C> proxyLookup(
        lookup: BlockApiLookup<A, C>,
        extendedConduitData: AE2InWorldConduitNodeHost,
        world: World,
        pos: BlockPos,
        direction: Direction?,
        state: InWorldNode.IOState?
    ): A? {
        if (lookup === getLookup()) {
            return extendedConduitData as A
        }
        return null
    }

    override fun getConduitItem(): Item {
        if (dense) return AE2Integration.DENSE_ITEM()
        return AE2Integration.NORMAL_ITEM()
    }

    protected fun getLookup(): BlockApiLookup<IInWorldGridNodeHost, Void> = IInWorldGridNodeHost.LOOKUP
}