package eyeq.tamablercreeper.entity.ai;

import eyeq.tamablercreeper.entity.passive.EntityCreeperTamable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAICreeperTamableSwell extends EntityAIBase {
    protected final EntityCreeperTamable entity;
    protected EntityLivingBase target;

    public EntityAICreeperTamableSwell(EntityCreeperTamable entity) {
        this.entity = entity;
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        EntityLivingBase target = this.entity.getAttackTarget();
        return this.entity.getCreeperState() > 0 || target != null && this.entity.getDistanceSqToEntity(target) < 9.0;
    }

    public void startExecuting() {
        this.entity.getNavigator().clearPathEntity();
        this.target = this.entity.getAttackTarget();
    }

    public void resetTask() {
        this.target = null;
    }

    public void updateTask() {
        if(this.target == null) {
            this.entity.setCreeperState(-1);
        } else if(this.entity.getDistanceSqToEntity(this.target) > 49.0) {
            this.entity.setCreeperState(-1);
        } else if(!this.entity.getEntitySenses().canSee(this.target)) {
            this.entity.setCreeperState(-1);
        } else {
            this.entity.setCreeperState(1);
        }
    }
}
