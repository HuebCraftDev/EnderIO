package de.huebcraft.mods.enderio.conduits.conduit.type

import de.huebcraft.mods.enderio.conduits.init.ConduitTypes
import net.minecraft.util.Identifier
import java.util.Comparator

object ConduitTypeSorter {
    private val SORTED_TYPES = arrayListOf<IConduitType<*>>()

    fun afterRegistryFreeze() {
        val registry = ConduitTypes.REGISTRY
        val tieredTypes = mutableListOf<Identifier>()
        for (type in registry) {
            if (type is TieredConduit<*> && !tieredTypes.contains(type.type)) tieredTypes.add(type.type)
        }
        tieredTypes.sortWith(Identifier::compareTo)
        for (tieredType in tieredTypes) {
            val typesInType = mutableListOf<IConduitType<*>>()
            for (type in registry) {
                if (type is TieredConduit<*> && type.type == tieredType) {
                    typesInType.add(type)
                }
            }
            typesInType.sortWith(Comparator.comparing(registry::getId))
            SORTED_TYPES.addAll(typesInType)
        }
        val remaining = mutableListOf<IConduitType<*>>()
        for (type in registry) {
            if (type !is TieredConduit) remaining.add(type)
        }
        remaining.sortWith(Comparator.comparing(registry::getId))
        SORTED_TYPES.addAll(remaining)
    }

    fun getSortIndex(type: IConduitType<*>): Int = SORTED_TYPES.indexOf(type)
}