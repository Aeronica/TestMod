package tld.testmod.client;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import tld.testmod.init.ModItems;
import tld.testmod.library.LocationArea;
import tld.testmod.common.items.IPlaceableBounding;

/*
 * box Rendering code mechanics by thebrightspark from the mod StructuralRelocation
 * https://github.com/thebrightspark/StructuralRelocation
 * 
 *                  GNU GENERAL PUBLIC LICENSE
 *                     Version 2, June 1991
 *
 * Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler
{
    private static Minecraft mc = Minecraft.getMinecraft();

    private static ItemStack getHeldSelector()
    {
        Iterator<ItemStack> heldItems = mc.player.getHeldEquipment().iterator();
        while(heldItems.hasNext())
        {
            ItemStack held = heldItems.next();
            if(!held.isEmpty() && held.getItem() instanceof IPlaceableBounding)
                return held;
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("unused")
    private static void renderBox(BlockPos pos, double partialTicks)
    {
        renderBox(new AxisAlignedBB(pos).grow(-0.00625d), partialTicks);
    }

    private static void renderBox(BlockPos pos1, BlockPos pos2, double partialTicks)
    {
        renderBox(new AxisAlignedBB(pos1, pos2).grow(-0.00625d), partialTicks);
    }

    private static void renderBox(AxisAlignedBB box, double partialTicks)
    {
        //Get player's actual position
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        //Render the box
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(4f);
        GlStateManager.disableTexture2D();
        GlStateManager.translate(-x, -y, -z);
        RenderGlobal.drawSelectionBoundingBox(box, 0f, 1f, 1f, 0.4f);
        RenderGlobal.renderFilledBox(box, 0f, 1f, 1f, 0.2f);
        RenderGlobal.drawSelectionBoundingBox(box, 0f, 1f, 1f, 0.4f);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void renderSelection(RenderWorldLastEvent event)
    {
        //Get held Selector item
        ItemStack heldItem = getHeldSelector();
        if(heldItem.isEmpty()) return;
        
        IPlaceableBounding blockPlacer = (IPlaceableBounding) heldItem.getItem();
        EntityPlayerSP player = mc.player;
        World world = player.getEntityWorld();

        RayTraceResult rayTraceResult = ForgeHooks.rayTraceEyes(player, 5);

        if(rayTraceResult != null && rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && rayTraceResult.sideHit.equals(EnumFacing.UP))
        {
            BlockPos pos = rayTraceResult.getBlockPos();
            if (blockPlacer.canPlaceHere(player, world, heldItem, pos, rayTraceResult.sideHit))
            {                
                LocationArea boundingBox = blockPlacer.getBoundingBox(player, world, pos);
                renderBox(boundingBox.getStartingPoint(), boundingBox.getStartPointPlusSize(), event.getPartialTicks());            
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModItems.ITEM_MULTI_TEX))

        {
            event.getToolTip().add(TextFormatting.GREEN + I18n.format("item.testmod:multitex.help"));
        }
    }
}
