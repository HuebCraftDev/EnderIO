package de.huebcraft.mods.enderio.conduits.conduit

import de.huebcraft.mods.enderio.conduits.conduit.connection.IConnectionState
import de.huebcraft.mods.enderio.conduits.conduit.type.IConduitType
import de.huebcraft.mods.enderio.conduits.math.Area
import net.minecraft.block.Block
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import kotlin.jvm.optionals.getOrNull

class ConduitShape {
    companion object {
        private val CONNECTOR: VoxelShape = Block.createCuboidShape(2.5, 2.5, 15.0, 13.5, 13.5, 16.0)
        private val CONNECTION: VoxelShape = Block.createCuboidShape(6.5, 6.5, 9.5, 9.5, 9.5, 16.0)
        private val CORE: VoxelShape = Block.createCuboidShape(6.5, 6.5, 6.5, 9.5, 9.5, 9.5)

        fun rotateVoxelShape(toRotate: VoxelShape, direction: Direction): VoxelShape {
            val buffer = arrayOf(toRotate, VoxelShapes.empty())
            if (direction.horizontal == -1) {
                if (direction === Direction.DOWN) {
                    buffer[0].forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                        buffer[1] =
                            VoxelShapes.union(buffer[1], VoxelShapes.cuboid(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY))
                    }
                } else {
                    buffer[0].forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                        buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(minX, minZ, minY, maxX, maxZ, maxY))
                    }
                }
                return buffer[1]
            }
            for (i in 0 until direction.horizontal % 4) {
                buffer[0].forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                    buffer[1] =
                        VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
                }
                buffer[0] = buffer[1]
                buffer[1] = VoxelShapes.empty()
            }
            return buffer[0]
        }
    }

    private val conduitShapes = mutableMapOf<IConduitType<*>, VoxelShape>()
    private val directionShapes = mutableMapOf<Direction, VoxelShape>()
    var combinedShape = CORE
        private set

    fun updateConduit(bundle: ConduitBundle) {
        conduitShapes.clear()
        directionShapes.clear()
        for (type in bundle.types) {
            updateShapeForConduit(bundle, type)
        }
        updateCombinedShape()
    }

    fun getShapeFromHit(pos: BlockPos, hitResult: HitResult): VoxelShape =
        conduitShapes[getConduit(pos, hitResult)] ?: VoxelShapes.empty()

    fun getConduit(pos: BlockPos, hitResult: HitResult): IConduitType<*>? =
        getLookupValue(conduitShapes, pos, hitResult)

    fun getDirection(pos: BlockPos, hitResult: HitResult): Direction? = getLookupValue(directionShapes, pos, hitResult)

    private fun <T> getLookupValue(shapes: Map<T, VoxelShape>, pos: BlockPos, hitResult: HitResult): T? {
        for ((element, shape) in shapes) {
            val vec = hitResult.pos.subtract(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            val point = shape.getClosestPointTo(vec).getOrNull() ?: continue
            if (point.isInRange(vec, MathHelper.EPSILON.toDouble())) return element
        }
        return null
    }

    private fun updateCombinedShape() {
        combinedShape = VoxelShapes.empty()
        conduitShapes.values.forEach { combinedShape = VoxelShapes.combine(combinedShape, it, BooleanBiFunction.OR) }
        combinedShape.simplify()
    }

    private fun updateShapeForConduit(bundle: ConduitBundle, conduitType: IConduitType<*>) {
        var shape = VoxelShapes.empty()
        val axis = OffsetHelper.findMainAxis(bundle)
        val offsets = mutableMapOf<IConduitType<*>, MutableList<Vec3i>>()
        for (direction in Direction.entries) {
            var directionShape = directionShapes.getOrDefault(direction, VoxelShapes.empty())
            if (bundle.getConnection(direction)
                    .getConnectionState(conduitType) is IConnectionState.DynamicConnectionState
            ) {
                val connectorShape = rotateVoxelShape(CONNECTOR, direction)
                directionShape = VoxelShapes.combine(directionShape, connectorShape, BooleanBiFunction.OR)
                shape = VoxelShapes.combine(shape, connectorShape, BooleanBiFunction.OR)
            }
            val connectedTypes = bundle.getConnection(direction).getConnectedTypes()
            if (connectedTypes.contains(conduitType)) {
                val offset = OffsetHelper.translationFor(
                    direction.axis, OffsetHelper.offsetConduit(connectedTypes.indexOf(conduitType), connectedTypes.size)
                )
                offsets.computeIfAbsent(conduitType) { arrayListOf() }.add(offset)
                val connectionShape = rotateVoxelShape(CONNECTION, direction).offset(
                    offset.x * 3.0 / 16.0, offset.y * 3.0 / 16.0, offset.z * 3.0 / 16.0
                )
                directionShape = VoxelShapes.combine(directionShape, connectionShape, BooleanBiFunction.OR)
                shape = VoxelShapes.combine(shape, connectionShape, BooleanBiFunction.OR)
            }
            directionShapes[direction] = directionShape.simplify()
        }

        val allTypes = bundle.types
        var box: Area? = null
        var notRendered: IConduitType<*>? = null
        val i = allTypes.indexOf(conduitType)
        if (i == -1) {
            conduitShapes[conduitType] = VoxelShapes.fullCube()
            return
        }
        val type = allTypes[i]
        val offsetsForType = offsets[type]
        if (offsetsForType != null) {
            if (offsetsForType.distinct().count() != 1) {
                box = Area(offsetsForType)
            }
        } else {
            notRendered = type
        }

        if (offsetsForType != null && (box == null || !box.contains(offsetsForType[0]))) {
            shape = VoxelShapes.combine(
                shape, CORE.offset(
                    offsetsForType[0].x * 3.0 / 16.0, offsetsForType[0].y * 3.0 / 16.0, offsetsForType[0].z * 3.0 / 16.0
                ), BooleanBiFunction.OR
            )
        }
        if (box != null) {
            shape = VoxelShapes.combine(
                shape, CORE.offset(
                    box.min.x * 3.0 / 16.0, box.min.y * 3.0 / 16.0, box.min.z * 3.0 / 16.0
                ), BooleanBiFunction.OR
            )
        } else {
            if (notRendered != null) {
                val offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allTypes.size))
                shape = VoxelShapes.combine(
                    shape, CORE.offset(
                        offset.x * 3.0 / 16.0, offset.y * 3.0 / 16.0, offset.z * 3.0 / 16.0
                    ), BooleanBiFunction.OR
                )
            }
        }

        conduitShapes[conduitType] = shape.simplify()
    }
}