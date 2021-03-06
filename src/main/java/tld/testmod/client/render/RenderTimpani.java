package tld.testmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import tld.testmod.Main;
import tld.testmod.client.model.ModelTimpani;
import tld.testmod.common.entity.living.EntityTimpani;

public class RenderTimpani extends RenderLiving<EntityTimpani>
{

    private static final ResourceLocation MOB_TIMPANI_TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/entity/timpani/mob_timpani.png");
    
    public static final IRenderFactory<EntityTimpani> FACTORY = (RenderManager manager) -> new RenderTimpani(manager);
    
    public RenderTimpani(RenderManager manager, ModelBase modelbase, float shadowsize)
    {
        super(manager, new ModelTimpani(), 0.25F);
    }

    public RenderTimpani(RenderManager manager)
    {
        super(manager,  new ModelTimpani(), 0.25F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(EntityTimpani entity)
    {
        return MOB_TIMPANI_TEXTURE;
    }

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    @Override
    protected void preRenderCallback(EntityTimpani entitylivingbaseIn, float partialTickTime)
    {
        int i = entitylivingbaseIn.getSlimeSize();
        float f = (entitylivingbaseIn.prevSquishFactor + (entitylivingbaseIn.squishFactor - entitylivingbaseIn.prevSquishFactor) * partialTickTime) / ((float)i * 0.5F + 1.0F);
        float f1 = 1.0F / (f + 1.0F);
        GlStateManager.scale(f1 * (float)i * 0.6F, 1.0F / f1 * (float)i * 0.6F, f1 * (float)i * 0.6F);
    }

}
