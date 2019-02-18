package thebetweenlands.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thebetweenlands.client.render.model.armor.ModelSpiritTreeFaceMaskLarge;
import thebetweenlands.client.render.model.armor.ModelSpiritTreeFaceMaskSmall;
import thebetweenlands.common.entity.EntitySpiritTreeFaceMask;
import thebetweenlands.common.lib.ModInfo;

@OnlyIn(Dist.CLIENT)
public class RenderSpiritTreeFaceMask extends Render<EntitySpiritTreeFaceMask> {
	private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation(ModInfo.ID, "textures/entity/spirit_tree_face_large.png");
	private static final ResourceLocation TEXTURE_SMALL = new ResourceLocation(ModInfo.ID, "textures/entity/spirit_tree_face_small.png");

	private static final ModelSpiritTreeFaceMaskLarge MODEL_LARGE = new ModelSpiritTreeFaceMaskLarge(false);
	private static final ModelSpiritTreeFaceMaskSmall MODEL_SMALL = new ModelSpiritTreeFaceMaskSmall(false);

	public RenderSpiritTreeFaceMask(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntitySpiritTreeFaceMask entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.rotatef(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(-1, -1, 1);

		this.bindEntityTexture(entity);

		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		ModelBase model = entity.getType() == EntitySpiritTreeFaceMask.Type.LARGE ? MODEL_LARGE : MODEL_SMALL;

		if(entity.getType() == EntitySpiritTreeFaceMask.Type.LARGE) {
			GlStateManager.translatef(0, -0.4F, 0);
		} else {
			GlStateManager.translatef(0, -1.1F, -0.5F);
		}

		model.render(entity, 0, 0, 0, 0, 0, 0.0625F);

		if (this.renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpiritTreeFaceMask entity) {
		return entity.getType() == EntitySpiritTreeFaceMask.Type.LARGE ? TEXTURE_LARGE : TEXTURE_SMALL;
	}
}