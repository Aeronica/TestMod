package tld.testmod.common.world;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.IWorldGenerator;
import tld.testmod.Main;
import tld.testmod.ModLogger;
import tld.testmod.common.entity.living.EntityGoldenSkeleton;
import tld.testmod.common.entity.living.EntityTimpani;
import tld.testmod.init.ModLootTables;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static net.minecraft.init.Biomes.*;

/**
 * {@link IWorldGenerator}
 * @see <a ref="https://www.reddit.com/r/feedthebeast/comments/5x0twz/investigating_extreme_worldgen_lag/">Reddit - investigating_extreme_worldgen_lag</a>
 * 
 */
public class WorldGenStageRegal implements IWorldGenerator
{

    private static final ResourceLocation STAGE_REGAL = new ResourceLocation(Main.prependModID("stage_regal"));

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if(!(world instanceof WorldServer))
            return;
        
        WorldServer worldServer = (WorldServer) world;
        // https://www.reddit.com/r/feedthebeast/comments/5x0twz/investigating_extreme_worldgen_lag/
        int x = chunkX * 16 + 8; // The all important offset of +8
        int z = chunkZ * 16 + 8; // The all important offset of +8

        BlockPos xzPos = new BlockPos(x, 1, z);
        Biome biome = worldServer.getBiomeForCoordsBody(xzPos);
        if(biome != HELL && biome != VOID && biome != ROOFED_FOREST
                && biome != MUSHROOM_ISLAND && biome != MUSHROOM_ISLAND_SHORE
                && biome != RIVER && biome != BEACH && worldServer.getVillageCollection().getNearestVillage(xzPos, 100) == null)
        {
            if(random.nextInt(2) == 0) {
                for (int rotation = random.nextInt(2) * 2; rotation < Rotation.values().length; rotation++)
                    if(generateStageAt(worldServer, rotation, random, x, z))
                        break;
            }
        }
    }

    @SuppressWarnings("static-access")
    private static boolean generateStageAt(WorldServer world, int rotation, Random random, int xIn, int zIn)
    {
        final PlacementSettings settings = new PlacementSettings().setRotation(Rotation.values()[rotation]);
        final Template template = world.getSaveHandler().getStructureTemplateManager().getTemplate(world.getMinecraftServer(), STAGE_REGAL);      
                    
        int i = xIn; // - template.getSize().getX()/2; // + random.nextInt(16);
        int k = zIn; // - template.getSize().getZ()/2; // + random.nextInt(16); 
        
        int airCount = 0;
        
        BlockPos size = template.transformedSize(Rotation.values()[rotation]);
        
        int agl = StructureHelper.getAverageGroundLevel(world, new StructureBoundingBox(new Vec3i(i-8, 64, k-8), new Vec3i(i+8, 128, k+8)),
                new StructureBoundingBox(new Vec3i(i-size.getX()/2, 64, k-size.getZ()/2), new Vec3i(i+size.getX()/2, 64, k+size.getZ()/2)));
        BlockPos zeroPos = template.getZeroPositionWithTransform(new BlockPos(i,agl,k).add(-size.getX()/2, 0, -size.getZ()/2), Mirror.NONE, settings.getRotation());
        
        int horizontalArea = size.getX() * size.getZ();
        
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for(int y = 0; y < template.getSize().getY(); y++)
            for(int x = 0; x < template.getSize().getX(); x++)
                for(int z = 0; z < template.getSize().getZ(); z++)
                {
                    BlockPos checkPos = template.transformedBlockPos(settings, blockpos$mutableblockpos.setPos(x, y, z)).add(zeroPos);
                    IBlockState checkState = world.getBlockState(checkPos);
                    IBlockState checkStateDown = world.getBlockState(checkPos.down());
                    if(!(checkState.getBlock() instanceof BlockAir))
                    {
                        // ModLogger.info("stage_regal OBSTRUCTED");
                        return false; // Obstructed, can't generate here
                    }
                    if(y == 0 && (checkStateDown.getMaterial().isLiquid() ||
                            checkState.getBlock().isFoliage(world, checkPos.down()) ||                            
//                            (checkStateDown.getBlock() instanceof BlockLeaves) ||
//                            (checkStateDown.getBlock() instanceof BlockLog) ||
//                            (checkStateDown.getBlock() instanceof BlockPlanks) ||
                            (checkStateDown.getBlock() instanceof BlockSlab)) )
                    {
                        // ModLogger.info("stage_regal NOT ON TREES OR WATER: block: %s", checkStateDown.getBlock().getRegistryName()); 
                        return false; // No spawning in trees, or on water!!
                    }
                }
        if(StructureHelper.canPlaceStage(zeroPos))
        {
            ModLogger.info("*** Stage_Regal ***: position %s", zeroPos.toString());
            template.addBlocksToWorld(world, zeroPos, settings);

            // Fill in below the structure with stone
            for(int z = 0; z < template.getSize().getZ(); z++)
                for(int x = 0; x < template.getSize().getX(); x++)
                    for (int y = -1 ; y > -zeroPos.getY()  ; y--)
                    {
                        BlockPos checkPos = template.transformedBlockPos(settings, blockpos$mutableblockpos.setPos(x, y, z)).add(zeroPos);
                        IBlockState checkState = world.getBlockState(checkPos);
                        if(checkState.getBlock().canPlaceBlockAt(world, checkPos) || !checkState.getBlock().isCollidable()
                                || (checkState.getBlock() instanceof BlockAir) || !checkState.getBlock().isPassable(world, checkPos)
                                || (checkState.getBlock() instanceof IGrowable) || (checkState.getBlock() instanceof IPlantable)) 
                            world.setBlockState(checkPos, Blocks.STONE.getDefaultState());
                            //world.setBlockState(checkPos, Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE));
                            //Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.CHISELED);
                    }
            replaceDataBlocks(world, zeroPos, random, template, settings);
            return true;
        }
        else
        {
            ModLogger.info("stage_regal TOO CLOSE TOGETHER");
            return false;
        }
    }
  
    private static void replaceDataBlocks(World worldIn, BlockPos posIn, Random randomIn, Template templateIn, PlacementSettings settingsIn)
    {
        Map<BlockPos, String> dataBlocks = templateIn.getDataBlocks(posIn, settingsIn);
        for(Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
            String[] tokens = entry.getValue().split(" ");
            if(tokens.length == 0)
                return;

            BlockPos dataPos = entry.getKey();
            EntityGoldenSkeleton skeleton;
            EntityTimpani  timpano;

            ModLogger.info("stage_regal dataEntry: %s", entry);
            switch(tokens[0])
            {
            case "skeleton":
                skeleton = new EntityGoldenSkeleton(worldIn);
                skeleton.setPosition(dataPos.getX() + 0.5, dataPos.getY() + 0.1, dataPos.getZ() + 0.5);
                skeleton.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
                skeleton.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                skeleton.setArrowCountInEntity(64);
                skeleton.setHomePosAndDistance(dataPos.add(0.5,0,0.5), 25);
                worldIn.spawnEntity(skeleton);
                ModLogger.info("stage_regal Skeleton: %s", skeleton);
                break;
            case "timpano":
                int size;
                timpano = new EntityTimpani(worldIn);
                timpano.setPosition(dataPos.getX() + 0.5, dataPos.getY() + 0.1, dataPos.getZ() + 0.5);
                try {
                    size = Integer.parseInt(tokens[1]);
                }
                catch (NumberFormatException e)
                {
                    size = 3;
                }
                if (size > 3 || size < 1)
                    size = 3;
                timpano.setSlimeSize(size, true);
                worldIn.spawnEntity(timpano);
                ModLogger.info("stage_regal Timpano: %s", timpano);
                break;
            case "loot":
                float chance = tokens.length == 3 ? 1F : 0.75F;

                if(randomIn.nextFloat() <= chance)
                {
                    String chestOrientation = tokens[1];
                    EnumFacing chestFacing = settingsIn.getRotation().rotate(EnumFacing.byName(chestOrientation));
                    IBlockState chestState = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, chestFacing);
                    worldIn.setBlockState(dataPos, chestState);

                    TileEntity tile = worldIn.getTileEntity(dataPos);
                    if(tile instanceof TileEntityLockableLoot)
                        ((TileEntityLockableLoot) tile).setLootTable(ModLootTables.STAGE_REGAL_CHEST_LOOT_TABLE, randomIn.nextLong());
                }
                else
                {
                    worldIn.setBlockState(dataPos, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.RED));
                }
                break;
            case "update": // Lighting update - Schedule a block update at the block below the DATA structure block - must be a mutable block and not AIR
//                worldIn.scheduleBlockUpdate(dataPos.down(), worldIn.getBlockState(dataPos.down()).getBlock(), 10, 0);
                break;
            }
        }   
    }
}
