package eyeq.tamablercreeper.entity.ai;

import eyeq.tamablercreeper.entity.passive.EntityCreeperTamable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityAICreeperTamableBeg extends EntityAIBase {
    private final EntityCreeperTamable entity;
    private final World world;
    private final float minPlayerDistance;
    private EntityPlayer player;
    private int timeoutCounter;

    public EntityAICreeperTamableBeg(EntityCreeperTamable entity, float minDistance) {
        this.entity = entity;
        this.world = entity.world;
        this.minPlayerDistance = minDistance;
        this.setMutexBits(2);
    }

    public boolean shouldExecute() {
        this.player = this.world.getClosestPlayerToEntity(this.entity, this.minPlayerDistance);
        if(this.player == null) {
            return false;
        }
        return this.hasTemptationItemInHand(this.player);
    }

    public boolean continueExecuting() {
        if(!this.player.isEntityAlive()) {
            return false;
        }
        if(this.entity.getDistanceSqToEntity(this.player) > this.minPlayerDistance * this.minPlayerDistance) {
            return false;
        }
        return this.timeoutCounter > 0 && this.hasTemptationItemInHand(this.player);
    }

    public void startExecuting() {
        this.entity.setBegging(true);
        this.timeoutCounter = 40 + this.entity.getRNG().nextInt(40);
    }

    public void resetTask() {
        this.entity.setBegging(false);
        this.player = null;
    }

    public void updateTask() {
        this.entity.getLookHelper().setLookPosition(this.player.posX, this.player.posY + this.player.getEyeHeight(), this.player.posZ, 10.0F, this.entity.getVerticalFaceSpeed());
        this.timeoutCounter--;
    }

    private boolean hasTemptationItemInHand(EntityPlayer player) {
        if(this.entity.isTamed()) {
            return false;
        }
        for(EnumHand enumhand : EnumHand.values()) {
            ItemStack itemStack = player.getHeldItem(enumhand);
            if(itemStack.getItem() == Item.getItemFromBlock(Blocks.TNT)) {
                return true;
            }
            if(this.entity.isBreedingItem(itemStack)) {
                return true;
            }
        }
        return false;
    }
}
