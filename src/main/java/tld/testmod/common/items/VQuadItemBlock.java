package tld.testmod.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tld.testmod.library.LocationArea;
import tld.testmod.common.blocks.VQuadBlock;

public class VQuadItemBlock extends Item implements IPlaceableBounding
{
    
    protected Block placedBlock;
    public VQuadItemBlock(Block blockIn)
    {
        placedBlock = blockIn;
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos posIn, EnumHand handIn, EnumFacing facingIn, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            /** Client side so just return */
            return EnumActionResult.SUCCESS;
        } else if (facingIn != EnumFacing.UP)
        {
            /** Can't place the blocks this way */
            return EnumActionResult.FAIL;
        } else
        {
            ItemStack stack = playerIn.getHeldItem(handIn);
            BlockPos posLL = posIn;
            
            IBlockState iblockstate = worldIn.getBlockState(posLL);
            Block block = iblockstate.getBlock();
            
            /** Looking at the ground or a replaceable block like grass. */
            boolean flagLL = block.isReplaceable(worldIn, posLL);
            if (!flagLL) posLL = posLL.up();
            
            int i = MathHelper.floor((double) (playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing enumfacing = EnumFacing.byHorizontalIndex(i);
            
            BlockPos posLR = posLL.offset(enumfacing.rotateY());
            BlockPos posUL = posLL.up();
            BlockPos posUR = posUL.offset(enumfacing.rotateY());
              
            if (canPlaceHere(playerIn, worldIn, stack, posIn, facingIn))
            {
                IBlockState iblockstate1 = placedBlock.getDefaultState().withProperty(VQuadBlock.FACING, enumfacing)
                        .withProperty(VQuadBlock.PART, VQuadBlock.EnumPartType.LL);

                if (worldIn.setBlockState(posLL, iblockstate1, 11))
                {
                    IBlockState iblockstate2 = iblockstate1.withProperty(VQuadBlock.PART, VQuadBlock.EnumPartType.LR);
                    if (worldIn.setBlockState(posLR, iblockstate2, 11))
                    {
                        IBlockState iblockstate3 = iblockstate2.withProperty(VQuadBlock.PART, VQuadBlock.EnumPartType.UL);
                        if (worldIn.setBlockState(posUL, iblockstate3, 11))
                        {
                            IBlockState iblockstate4 = iblockstate2.withProperty(VQuadBlock.PART, VQuadBlock.EnumPartType.UR);
                            worldIn.setBlockState(posUR, iblockstate4, 11);
                        }
                    }
                }
                SoundType soundtype = iblockstate1.getBlock().getSoundType();
                worldIn.playSound((EntityPlayer) null, posLL, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.setCount(stack.getCount()-1);
                return EnumActionResult.SUCCESS;
            } else
            {
                return EnumActionResult.FAIL;
            }
        }
    }

    public boolean canPlaceHere(EntityPlayer playerIn, World worldIn, ItemStack stack, BlockPos posIn, EnumFacing facingIn)
    {
        BlockPos posLL = posIn;
        IBlockState iblockstate = worldIn.getBlockState(posLL);
        Block block = iblockstate.getBlock();
        /** Looking at the ground or a replaceable block like grass. */
        boolean flagLL = block.isReplaceable(worldIn, posLL);
        if (!flagLL) posLL = posLL.up();

        /**determine the direction the player is facing */
        int i = MathHelper.floor((double) (playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(i);
        /**get the next blocks. */
        BlockPos posLR = posLL.offset(enumfacing.rotateY());
        BlockPos posUL = posLL.up();
        BlockPos posUR = posUL.offset(enumfacing.rotateY());

        if (playerIn.canPlayerEdit(posLL, facingIn, stack) && playerIn.canPlayerEdit(posLR, facingIn, stack) &&
                playerIn.canPlayerEdit(posUL, facingIn, stack) && playerIn.canPlayerEdit(posUR, facingIn, stack))
        {
            boolean flagLR = worldIn.getBlockState(posLR).getBlock().isReplaceable(worldIn, posLR);
            boolean flagUL = worldIn.getBlockState(posUL).getBlock().isReplaceable(worldIn, posUL);
            boolean flagUR = worldIn.getBlockState(posUR).getBlock().isReplaceable(worldIn, posUR);
            flagLL |= worldIn.isAirBlock(posLL);
            flagLR |= worldIn.isAirBlock(posLR);
            flagUL |= worldIn.isAirBlock(posUL);
            flagUR |= worldIn.isAirBlock(posUR);
            
            /** Disallow placing blocks on water or other unstable blocks */
            if (flagLL && flagLR && flagUL && flagUR && worldIn.getBlockState(posLL.down()).isFullCube() && worldIn.getBlockState(posLR.down()).isFullCube())
            {
                return true;
            }
        }
        return false;
    }
    
    public LocationArea getBoundingBox(EntityPlayer playerIn, World worldIn, BlockPos posIn)
    {
        BlockPos pos = posIn;
        int i = MathHelper.floor((double) (playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(i);
        
        Block block = worldIn.getBlockState(pos).getBlock();
        
        /** Looking at the ground or a replaceable block like grass. */
        if (!block.isReplaceable(worldIn, pos)) pos = pos.up();
        /** The bounding box area - vertical 4x4 */
        return new LocationArea(pos, pos.offset(enumfacing.rotateY()).up());
    }
}
