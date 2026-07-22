package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import java.util.UUID;

public class ZombieCommanderMaggotEntity extends PathfinderMob implements IZombieTribe {
    private UUID ownerUUID;
    private int lifeTicks = 200;

    public ZombieCommanderMaggotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public void setOwner(UUID uuid) { this.ownerUUID = uuid; }

    // 💡 수명 설정용 메서드 추가
    public void setLifeTicks(int ticks) { this.lifeTicks = ticks; }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, e -> e instanceof IAlly));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            lifeTicks--;
            if (lifeTicks <= 0) this.discard();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean flag = target.hurt(net.minecraft.world.damagesource.DamageSource.mobAttack(this), damage);
        if (flag && !this.level.isClientSide && ownerUUID != null) {
            Entity owner = ((net.minecraft.server.level.ServerLevel)this.level).getEntity(ownerUUID);
            if (owner instanceof ZombieCommanderEntity commander) {
                commander.recordAttackXP((double)damage);
            }
        }
        return flag;
    }
}