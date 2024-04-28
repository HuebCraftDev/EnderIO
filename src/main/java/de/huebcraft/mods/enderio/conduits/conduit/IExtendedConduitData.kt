package de.huebcraft.mods.enderio.conduits.conduit

import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

interface IExtendedConduitData<T : IExtendedConduitData<T>> {
    fun onCreated(type: IConduitType<*>, world: World, pos: BlockPos, playerEntity: PlayerEntity?) = Unit

    fun onRemoved(type: IConduitType<*>, world: World, pos: BlockPos) = Unit

    fun updateConnection(connectedSides: Set<Direction>) = Unit

    fun canConnectTo(other: T): Boolean = true

    fun onConnectTo(other: T) = Unit

    fun serializeRenderNbt(): NbtCompound = NbtCompound()

    fun readNbt(nbt: NbtCompound)

    fun writeNbt(): NbtCompound

    @Environment(EnvType.CLIENT)
    fun deepCopy(): T = this as T
}