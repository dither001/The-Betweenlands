package thebetweenlands.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import thebetweenlands.client.render.model.entity.ModelWight;
import thebetweenlands.common.entity.mobs.EntityPrimordialMalevolenceTurret;

public class RenderFortressBossTurret extends Render<EntityPrimordialMalevolenceTurret> {
	protected static final ModelWight MODEL = new ModelWight().setRenderHeadOnly(true);

	public RenderFortressBossTurret(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityPrimordialMalevolenceTurret entity, double x, double y, double z, float yaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.pushMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.translated(x, y, z);
		this.bindTexture(new ResourceLocation("thebetweenlands:textures/entity/wight.png"));
		GlStateManager.rotatef(180, 1, 0, 0);
		GlStateManager.rotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0, 1, 0);
		GlStateManager.rotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1, 0, 0);
		GlStateManager.translatef(0, 0, 0.25F);
		GlStateManager.translatef((float)Math.sin((entity.ticksExisted + partialTicks)/5.0D) * 0.1F, (float)Math.cos((entity.ticksExisted + partialTicks)/7.0D) * 0.1F, (float)Math.cos((entity.ticksExisted + partialTicks)/6.0D) * 0.1F);
		if(!entity.isObstructedByBoss()) {
			GlStateManager.color4f(1, 1, 1, 0.8F);
		} else {
			GlStateManager.color4f(1, 0.4F, 0.4F, 0.8F);
		}
		MODEL.render(entity, entity.distanceWalkedModified, 360, entity.ticksExisted + partialTicks, 0, 0, 0.065F);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPrimordialMalevolenceTurret p_110775_1_) {
		return null;
	}
}
