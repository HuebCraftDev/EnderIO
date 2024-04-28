package de.huebcraft.mods.enderio.conduits.math

import net.minecraft.util.math.Vec3i

class Area(min: Vec3i, max: Vec3i) {
    var min = min
        private set
    var max = max
        private set

    constructor(targets: List<Vec3i>) : this(Vec3i(targets.minBy { it.x }.x,
        targets.minBy { it.y }.y,
        targets.minBy { it.z }.z
    ), Vec3i(targets.maxBy { it.x }.x, targets.maxBy { it.y }.y, targets.maxBy { it.z }.z)
    )

    constructor(vararg targets: Vec3i) : this(targets.asList())

    fun makeContain(vec: Vec3i) {
        min = Vec3i(vec.x.coerceAtMost(min.x), vec.y.coerceAtMost(min.y), vec.z.coerceAtMost(min.z))
        max = Vec3i(vec.x.coerceAtLeast(max.x), vec.y.coerceAtLeast(max.y), vec.z.coerceAtLeast(max.z))
    }

    fun size(): Vec3i = Vec3i(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1)

    fun contains(vec: Vec3i): Boolean {
        if (min.x > vec.x || vec.x > max.x) return false
        if (min.y > vec.y || vec.y > max.y) return false
        return min.z <= vec.z && vec.z <= max.z
    }
}