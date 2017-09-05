package tld.testmod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import tld.testmod.common.entity.EntityPull;
import tld.testmod.init.ModItems;

public class RenderPullEntity<T extends Entity> extends Render<T>
{

    private static Minecraft mc = Minecraft.getMinecraft();
    public static final IRenderFactory<EntityPull> FACTORY = (RenderManager manager) -> new RenderPullEntity<EntityPull>(manager, ModItems.ITEM_PULL ,mc.getRenderItem());
    protected final Item item;
    private final RenderItem itemRenderer;
    
    protected RenderPullEntity(RenderManager renderManager, Item itemIn, RenderItem itemRendererIn)
    {
        super(renderManager);
        this.item = itemIn;
        this.itemRenderer = itemRendererIn;
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        this.itemRenderer.renderItem(this.getStackToRender(entity), ItemCameraTransforms.TransformType.GROUND);

        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public ItemStack getStackToRender(T entityIn)
    {
        return new ItemStack(this.item);
    }

    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
