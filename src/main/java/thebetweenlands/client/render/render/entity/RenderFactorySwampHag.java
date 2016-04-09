package thebetweenlands.client.render.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.common.entity.mobs.EntitySwampHag;

@SideOnly(Side.CLIENT)
public class RenderFactorySwampHag implements IRenderFactory<EntitySwampHag> {
    @Override
    public Render<? super EntitySwampHag> createRenderFor(RenderManager manager) {
        return new RenderSwampHag(manager);
    }
}
