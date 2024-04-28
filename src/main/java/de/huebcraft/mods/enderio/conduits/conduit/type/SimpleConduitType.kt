package de.huebcraft.mods.enderio.conduits.conduit.type

import de.huebcraft.mods.enderio.conduits.conduit.IClientConduitData
import de.huebcraft.mods.enderio.conduits.conduit.IConduitMenuData
import de.huebcraft.mods.enderio.conduits.conduit.ticker.IConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.joml.Vector2i

open class SimpleConduitType<T : IExtendedConduitData<T>>(
    val texture: Identifier,
    override val ticker: IConduitTicker,
    private val extendedDataFactory: () -> T,
    override val clientData: IClientConduitData<T>,
    override val menuData: IConduitMenuData
) : IConduitType<T> {

    constructor(
        texture: Identifier,
        ticker: IConduitTicker,
        extendedDataFactory: () -> T,
        iconTexture: Identifier,
        iconTexturePos: Vector2i,
        menuData: IConduitMenuData
    ) : this(texture, ticker, extendedDataFactory, IClientConduitData.Simple(iconTexture, iconTexturePos), menuData)

    override fun getTexture(extendedConduitData: T): Identifier = texture

    override val itemTexture: Identifier
        get() = texture

    override fun createExtendedConduitData(world: World, pos: BlockPos): T = extendedDataFactory()
}