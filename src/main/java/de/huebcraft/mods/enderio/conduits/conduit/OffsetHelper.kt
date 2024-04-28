package de.huebcraft.mods.enderio.conduits.conduit

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Vec3i
import org.joml.Vector2i

object OffsetHelper {
    val positions = mapOf(
        1 to Vector2i(0, -1),
        2 to Vector2i(-1, 0),
        3 to Vector2i(0, 1),
        4 to Vector2i(1, 0),
        5 to Vector2i(1, -1),
        6 to Vector2i(-1, -1),
        7 to Vector2i(-1, 1),
        8 to Vector2i(1, 1),
        9 to Vector2i(0, 0),
    )

    private val ZERO = Vector2i(0, 0)

    fun offsetConduit(typeIndex: Int, maxTypes: Int): Vector2i {
        if (typeIndex >= maxTypes) {
            return ZERO
        }
        if (typeIndex < 0) {
            return ZERO
        }
        if (maxTypes == 1) {
            return ZERO
        }
        if (maxTypes == 2) {
            return if (typeIndex == 0) Vector2i(0, -1) else Vector2i(0, 1)
        }
        if (maxTypes == 3) {
            return when (typeIndex) {
                0 -> Vector2i(-1, -1)
                1 -> ZERO
                2 -> Vector2i(1, 1)
                else -> throw IllegalStateException()
            }
        }
        if (maxTypes < 9) {
            return positions[typeIndex + 1] ?: ZERO
        }

        return ZERO
    }

    fun findMainAxis(bundle: ConduitBundle): Axis {
        val connectedDirections = mutableListOf<Direction>()
        for (direction in Direction.entries) {
            if (bundle.getConnection(direction).getConnectedTypes().isNotEmpty()) connectedDirections.add(direction)
        }
        if (connectedDirections.isEmpty()) return Axis.Z

        return connectedDirections[connectedDirections.size - 1].axis
    }

    fun translationFor(axis: Axis, offset: Vector2i): Vec3i {
        return when (axis) {
            Axis.X -> Vec3i(0, offset.y, offset.x)
            Axis.Y -> Vec3i(offset.x, 0, offset.y)
            Axis.Z -> Vec3i(offset.x, offset.y, 0)
        }
    }
}