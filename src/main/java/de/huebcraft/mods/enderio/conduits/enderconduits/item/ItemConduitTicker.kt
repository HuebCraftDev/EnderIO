package de.huebcraft.mods.enderio.conduits.enderconduits.item

import de.huebcraft.mods.enderio.conduits.conduit.ticker.LookupAwareConduitTicker
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import dev.gigaherz.graph3.Graph
import dev.gigaherz.graph3.Mergeable
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

@Suppress("UnstableApiUsage")
class ItemConduitTicker : LookupAwareConduitTicker<Storage<ItemVariant>>() {
    override fun tickLookupGraph(
        type: IConduitType<*>,
        inserts: List<LookupConnection>,
        extracts: List<LookupConnection>,
        world: ServerWorld,
        graph: Graph<Mergeable.Dummy>,
        isRedstoneActive: (ServerWorld, BlockPos, ColorControl) -> Boolean
    ) {
        nextExtract@ for (extractLookup in extracts) {
            val extractHandler = extractLookup.lookup
            for (slot in extractHandler) {
                if (slot.isResourceBlank) continue
                val amount = Transaction.openOuter().use {
                    val amount = slot.extract(slot.resource, 4, it)
                    it.abort()
                    amount
                }
                if (amount == 0L) {
                    continue
                }
                val sideData = (extractLookup.data as ItemExtendedData).compute(extractLookup.direction)
                if (sideData.roundRobin) {
                    if (inserts.size <= sideData.rotatingIndex) {
                        sideData.rotatingIndex = 0
                    }
                } else {
                    sideData.rotatingIndex = 0
                }

                for (j in sideData.rotatingIndex until sideData.rotatingIndex + inserts.size) {
                    val insertIdx = j % inserts.size
                    val insertLookup = inserts[insertIdx]

                    if (!sideData.selfFeed && extractLookup.direction === insertLookup.direction && extractLookup.data === insertLookup.data) {
                        continue
                    }

                    val success = Transaction.openOuter().use {
                        val inserted = insertLookup.lookup.insert(slot.resource, amount, it)
                        if (inserted > 0) {
                            val extracted = extractLookup.lookup.extract(slot.resource, inserted, it)
                            if (extracted != amount) {
                                it.abort()
                                return@use false
                            }
                            if (sideData.roundRobin) {
                                sideData.rotatingIndex += insertIdx + 1
                            }
                            it.commit()
                            return@use true
                        }
                        false
                    }
                    if (success) continue@nextExtract
                }
            }
        }
    }

    override fun getLookup(): BlockApiLookup<Storage<ItemVariant>, Direction?> = ItemStorage.SIDED

    override fun getTickRate(): Int = 20
}