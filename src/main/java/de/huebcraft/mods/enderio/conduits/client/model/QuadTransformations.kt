package de.huebcraft.mods.enderio.conduits.client.model

import de.huebcraft.mods.enderio.conduits.misc.ColorControl
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

object QuadTransformations {
    class QuadRotation(direction: Direction) : QuadTransform {

        private val rotation: Quaternionf = direction.opposite.rotationQuaternion

        /**
         * Thanks to ae2 for the code!
         */
        override fun transform(quad: MutableQuadView): Boolean {
            val tmp = Vector3f()
            for (i in 0..3) {
                quad.copyPos(i, tmp)
                tmp.add(-0.5f, -0.5f, -0.5f)
                tmp.rotate(rotation)
                tmp.add(0.5f, 0.5f, 0.5f)
                quad.pos(i, tmp)

                if (quad.hasNormal(i)) {
                    quad.copyNormal(i, tmp)
                    tmp.rotate(rotation)
                    quad.normal(i, tmp)
                }
            }

            return true
        }
    }

    class ConduitQuadTranslation(offset: Vec3i) : QuadTransform {
        private val scaledOffset = Vector3f(offset.x * (3f / 16f), offset.y * (3f / 16f), offset.z * (3f / 16f))

        override fun transform(quad: MutableQuadView): Boolean {
            val tmp = Vector3f()
            for (i in 0..3) {
                quad.copyPos(i, tmp)
                tmp.add(scaledOffset)
                quad.pos(i, tmp)
            }
            return true
        }
    }

    class ConduitTextureQuadTransformation(
        private val sprite: Sprite,
        minUv: Vector2f = Vector2f(0f, 0f),
        maxUv: Vector2f = Vector2f(1f, 1f)
    ) : QuadTransform {
        private val atlasMinU = MathHelper.lerp(minUv.x, sprite.minU, sprite.maxU)
        private val atlasMinV = MathHelper.lerp(minUv.y, sprite.minV, sprite.maxV)
        private val atlasMaxU = MathHelper.lerp(maxUv.x, sprite.minU, sprite.maxU)
        private val atlasMaxV = MathHelper.lerp(maxUv.y, sprite.minV, sprite.maxV)
        override fun transform(quad: MutableQuadView): Boolean {
            quad.spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED)
            quad.uv(0, atlasMinU, atlasMinV)
            quad.uv(1, atlasMinU, atlasMaxV)
            quad.uv(2, atlasMaxU, atlasMaxV)
            quad.uv(3, atlasMaxU, atlasMinV)
            return true
        }
    }

    class ColorQuadTransformation(private val insert: ColorControl?, private val extract: ColorControl?) :
        QuadTransform {
        override fun transform(quad: MutableQuadView): Boolean {
            if (quad.colorIndex() != -1) {
                if (quad.colorIndex() == 0 && extract != null) {
                    quad.color(extract.color, extract.color, extract.color, extract.color)
                } else if (quad.colorIndex() == 1 && insert != null) {
                    quad.color(insert.color, insert.color, insert.color, insert.color)
                }
            }
            return true
        }
    }

    class BoxTextureQuadTransformation(scaling: Vec3i) : QuadTransform {
        private val realScaling = Vector3f(scaling.x.toFloat(), scaling.y.toFloat(), scaling.z.toFloat())
        override fun transform(quad: MutableQuadView): Boolean {
            val tmp = Vector3f()
            for (i in 0..3) {
                quad.copyPos(i, tmp)
                tmp.mul(realScaling)
                tmp.add(6.5f / 16f, 6.5f / 16f, 6.5f / 16f)
                quad.pos(i, tmp)
            }
            return true
        }
    }
}