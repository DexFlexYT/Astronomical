package doctor4t.astronomical.client.render.world;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.sammy.lodestone.setup.LodestoneRenderLayers;
import com.sammy.lodestone.systems.rendering.VFXBuilders;
import com.sammy.lodestone.systems.rendering.particle.ParticleBuilders;
import doctor4t.astronomical.cca.AstraCardinalComponents;
import doctor4t.astronomical.common.init.ModParticles;
import doctor4t.astronomical.common.structure.CelestialObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

import static com.sammy.lodestone.handlers.RenderHandler.DELAYED_RENDER;

public class AstraSkyRenderer {
	private static final Color yeah = new Color(255, 107, 160);
	private static final Vec3d UP = new Vec3d(0, 1, 0);
	public static void renderSky(MatrixStack matrices, VertexConsumerProvider provider, Matrix4f projectionMatrix, Frustum frustum, float tickDelta, Runnable runnable, ClientWorld world, MinecraftClient client, VertexBuffer lightSkyBuffer, VertexBuffer darkSkyBuffer, VertexBuffer starsBuffer) {
		Quaternion rotation = Vec3f.POSITIVE_Z.getDegreesQuaternion(world.getSkyAngle(1) * 360.0F);
		rotation.normalize();

		matrices.push();
		matrices.multiply(rotation);

		VFXBuilders.WorldVFXBuilder builder = new AstraWorldVFXBuilder().setPosColorTexLightmapDefaultFormat();
		for(CelestialObject c : world.getComponent(AstraCardinalComponents.SKY).getCelestialObjects()) {
			Vec3d vector = c.getDirectionVector();

			Vec3f[] p = createVectorArray(vector, UP.multiply(-1), 160);
			if(shouldRender(((AstraFrustum)frustum), p, rotation))
				builder.setColor(yeah)
					.setAlpha(0.9f)
					.setUV(0, 0, 1, 1)
					.renderQuad(DELAYED_RENDER.getBuffer(LodestoneRenderLayers.ADDITIVE_TEXTURE.applyAndCache(c.getTexture())), matrices, p, 1);
//			ParticleBuilders.create(ModParticles.ORB)
//				.setScale((10f + world.random.nextFloat() / 10f))
//				.setAlpha(0, 1f, 0)
//				.enableNoClip()
//				.setLifetime(20)
//				.setSpin(-world.random.nextFloat() / 7f)
//				.spawn(world, pos.x, pos.y, pos.z);
		}
		matrices.pop();
	}
	private static boolean shouldRender(AstraFrustum a, Vec3f[] vecs, Quaternion q) {
		for(Vec3f vec : vecs) {
			Vec3f newVec = new Vec3f(vec.getX(), vec.getY(), vec.getZ());
            newVec.rotate(q);
			if(a.astra$isVisible(newVec.getX(), newVec.getY(), newVec.getZ())) {
				return true;
			}
		}
		return false;
	}

	private static Vec3f[] createVectorArray(Vec3d dir, Vec3d up, float distance) {
		float x = (float) dir.x;
		float y = (float) dir.y;
		float z = (float) dir.z;

		float ux = (float) up.x;
		float uy = (float) up.y;
		float uz = (float) up.z;

		float t1x = -y*uz+(z*uy);
		float t1y = -z*ux+(x*uz);
		float t1z = -x*uy+(y*ux);

		float t1d2 = (float) Math.sqrt(distanceSquared(t1x, t1y, t1z));

		t1x /= t1d2; t1y /= t1d2; t1z /= t1d2;

		float t2x = -y*t1z+(z*t1y);
		float t2y = -z*t1x+(x*t1z);
		float t2z = -x*t1y+(y*t1x);

		float t2d2 = (float) Math.sqrt(distanceSquared(t2x, t2y, t2z));

		t2x /= t2d2; t2y /= t2d2; t2z /= t2d2;

		x *= distance;
		y *= distance;
		z *= distance;

		return new Vec3f[]{new Vec3f(x + t1x + t2x, y + t1y + t2y, z + t1z + t2z), new Vec3f(x - t1x + t2x, y - t1y + t2y, z - t1z + t2z),  new Vec3f(x - t1x - t2x, y - t1y - t2y, z - t1z - t2z), new Vec3f(x + t1x - t2x, y + t1y - t2y, z + t1z - t2z)};
	}

	private static float distanceSquared(float x, float y, float z) {
		return x*x + y*y + z*z;
	}

	public static Vec3d rotateViaQuat(Vec3d rot, Quaternion quat) {
		Quaternion q = quat.copy();
		Quaternion qPrime = new Quaternion(-q.getX(), -q.getY(), -q.getZ(), q.getW());
		q.hamiltonProduct(new Quaternion((float)rot.getX(), (float)rot.getY(), (float)rot.getZ(), 0));
		q.hamiltonProduct(qPrime);
		return new Vec3d(q.getX(), q.getY(), q.getZ());
	}
}
