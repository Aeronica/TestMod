/**
 * Copyright {2016} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tld.testmod.common.entity.living.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;
import tld.testmod.common.entity.living.EntityGoldenSkeleton;

public class EntityGSAIAttackRangedBow extends EntityAIBase
{
    private final EntityGoldenSkeleton entity;
    private final double moveSpeedAmp;
    private int attackCooldown;
    private final float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public EntityGSAIAttackRangedBow(EntityGoldenSkeleton skeleton, double speedAmplifier, int delay, float maxDistance)
    {
        this.entity = skeleton;
        this.moveSpeedAmp = speedAmplifier;
        this.attackCooldown = delay;
        this.maxAttackDistance = maxDistance * maxDistance;
        this.setMutexBits(3);
    }

    public void setAttackCooldown(int p_189428_1_)
    {
        this.attackCooldown = p_189428_1_;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return this.entity.getAttackTarget() == null ? false : this.isBowInMainhand();
    }

    protected boolean isBowInMainhand()
    {
        return !this.entity.getHeldItemMainhand().isEmpty() && this.entity.getHeldItemMainhand().getItem() == Items.BOW;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isBowInMainhand();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        super.startExecuting();
        this.entity.setSwingingArms(true);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        super.resetTask();
        this.entity.setSwingingArms(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.entity.resetActiveHand();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        EntityLivingBase entitylivingbase = this.entity.getAttackTarget();

        if (entitylivingbase != null)
        {
            double d0 = this.entity.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            boolean flag = this.entity.getEntitySenses().canSee(entitylivingbase);
            boolean flag1 = this.seeTime > 0;

            if (flag != flag1)
            {
                this.seeTime = 0;
            }

            if (flag)
            {
                ++this.seeTime;
            }
            else
            {
                --this.seeTime;
            }

            if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20)
            {
                this.entity.getNavigator().clearPathEntity();
                ++this.strafingTime;
            }
            else
            {
                this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAmp);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20)
            {
                if ((double)this.entity.getRNG().nextFloat() < 0.3D)
                {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.entity.getRNG().nextFloat() < 0.3D)
                {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1)
            {
                if (d0 > (double)(this.maxAttackDistance * 0.75F))
                {
                    this.strafingBackwards = false;
                }
                else if (d0 < (double)(this.maxAttackDistance * 0.25F))
                {
                    this.strafingBackwards = true;
                }

                this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.entity.faceEntity(entitylivingbase, 30.0F, 30.0F);
            }
            else
            {
                this.entity.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
            }

            if (this.entity.isHandActive())
            {
                if (!flag && this.seeTime < -60)
                {
                    this.entity.resetActiveHand();
                }
                else if (flag)
                {
                    int i = this.entity.getItemInUseMaxCount();

                    if (i >= 20)
                    {
                        this.entity.resetActiveHand();
                        this.entity.attackEntityWithRangedAttack(entitylivingbase, ItemBow.getArrowVelocity(i));
                        this.attackTime = this.attackCooldown;
                    }
                }
            }
            else if (--this.attackTime <= 0 && this.seeTime >= -60)
            {
                this.entity.setActiveHand(EnumHand.MAIN_HAND);
            }
        }
    }
}