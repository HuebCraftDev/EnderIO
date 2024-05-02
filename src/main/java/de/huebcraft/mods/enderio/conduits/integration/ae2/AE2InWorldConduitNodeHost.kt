package de.huebcraft.mods.enderio.conduits.integration.ae2

import appeng.api.networking.*
import appeng.api.util.AECableType
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class AE2InWorldConduitNodeHost(private val conduitType: AE2ConduitType) : IInWorldGridNodeHost,
    IExtendedConduitData<AE2InWorldConduitNodeHost> {
    private var mainNode: IManagedGridNode? = null

    init {
        initMainNode()
    }

    fun initMainNode() {
        mainNode =
            GridHelper.createManagedNode(this, GridNodeListener()).setVisualRepresentation(conduitType.getConduitItem())
                .setInWorldNode(true).setTagName("conduit")
        mainNode!!.setIdlePowerUsage(if (conduitType.dense) 0.4 else 0.1)
        if (conduitType.dense)
            mainNode!!.setFlags(GridFlags.DENSE_CAPACITY)
    }

    override fun getGridNode(p0: Direction?): IGridNode? {
        if (mainNode == null) initMainNode()
        return mainNode!!.node
    }

    override fun getCableConnectionType(dir: Direction?): AECableType {
        if (conduitType.dense) return AECableType.DENSE_SMART
        return AECableType.SMART
    }

    override fun readNbt(nbt: NbtCompound) {
        if (mainNode == null) {
            initMainNode()
        }
        mainNode!!.loadFromNBT(nbt)
    }

    override fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        if (mainNode != null) mainNode!!.saveToNBT(nbt)
        return nbt
    }

    override fun onCreated(type: IConduitType<*>, world: World, pos: BlockPos, playerEntity: PlayerEntity?) {
        if (mainNode == null) initMainNode()

        if (mainNode!!.isReady) return

        if (playerEntity != null) mainNode!!.setOwningPlayer(playerEntity)

        GridHelper.onFirstTick(world.getBlockEntity(pos)) { mainNode!!.create(world, pos) }
    }

    override fun updateConnection(connectedSides: Set<Direction>) {
        if (mainNode == null) return

        mainNode!!.setExposedOnSides(connectedSides)
    }

    override fun onRemoved(type: IConduitType<*>, world: World, pos: BlockPos) {
        if (mainNode != null) {
            mainNode!!.destroy()
            mainNode = null
        }
    }
}