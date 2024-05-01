package de.huebcraft.mods.enderio.conduits.integration.ae2

import appeng.api.networking.IGridNode
import appeng.api.networking.IInWorldGridNodeHost
import de.huebcraft.mods.enderio.conduits.conduit.IExtendedConduitData
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

class AE2InWorldConduitNodeHost : IInWorldGridNodeHost, IExtendedConduitData<AE2InWorldConduitNodeHost> {
    override fun getGridNode(p0: Direction?): IGridNode? {
        TODO("Not yet implemented")
    }

    override fun readNbt(nbt: NbtCompound) {
        TODO("Not yet implemented")
    }

    override fun writeNbt(): NbtCompound {
        TODO("Not yet implemented")
    }
}