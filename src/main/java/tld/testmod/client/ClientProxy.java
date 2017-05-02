package tld.testmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import tld.testmod.client.render.RenderTestSkeleton;
import tld.testmod.common.CommonProxy;
import tld.testmod.common.entity.living.EntityGoldenSkeleton;

public class ClientProxy extends CommonProxy
{

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoldenSkeleton.class, new IRenderFactory<EntityGoldenSkeleton>() {
            @Override
            public Render<? super EntityGoldenSkeleton> createRenderFor(RenderManager manager) {
                return new RenderTestSkeleton(manager);
            }
        });
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);       
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);       
    }
    
    @Override
    public Side getPhysicalSide() {return Side.CLIENT;}

    @Override
    public Side getEffectiveSide() {return FMLCommonHandler.instance().getEffectiveSide();}

    @Override
    public Minecraft getMinecraft() {return Minecraft.getMinecraft();}

    @Override
    public EntityPlayer getClientPlayer() {return Minecraft.getMinecraft().player;}

    @Override
    public World getClientWorld() {return Minecraft.getMinecraft().world;}

    @Override
    public World getWorldByDimensionId(int dimension)
    {
        Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
        if (effectiveSide == Side.SERVER)
        {
            return FMLClientHandler.instance().getServer().worldServerForDimension(dimension);
        } else
        {
            return getClientWorld();
        }
    }
    
    @Override
    public boolean playerIsInCreativeMode(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            return entityPlayerMP.isCreative();
        } else if (player instanceof EntityPlayerSP)
        {
            return Minecraft.getMinecraft().playerController.isInCreativeMode();
        }
        return false;
    }

}