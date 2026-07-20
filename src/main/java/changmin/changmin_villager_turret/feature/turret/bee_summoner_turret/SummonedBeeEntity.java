package changmin.changmin_villager_turret.feature.turret.bee_summoner_turret;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

// 🛡️ 아군 등록 해결: IAlly 인터페이스를 구현하여 아군 터렛들의 폭격 유탄 등 오인 사격에 무적이 됩니다.
public class SummonedBeeEntity extends PathfinderMob implements IAlly {

    public static final int TICKS_PER_FLAP = 2;

    private int remainingLife = 400;
    private float attackDamage = 2.0F;
    private double searchRange = 16.0D;

    private int poisonLevel = 0;
    private int witherLevel = 0;
    private int slownessLevel = 0;
    private int weaknessLevel = 0;

    private BeeSummonerTurretEntity parentTurret;
    private LivingEntity priorityDefenseTarget;

    public SummonedBeeEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new BeeWanderGoal());

        this.targetSelector.addGoal(1, new BeeRetaliateGoal(this));
        this.targetSelector.addGoal(2, new BeeTargetHostileGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public boolean causeFallDamage(float p_147105_, float p_147106_, DamageSource p_147107_) {
        return false;
    }

    @Override
    protected void checkFallDamage(double p_20809_, boolean p_20810_, net.minecraft.world.level.block.state.BlockState p_20811_, BlockPos p_20812_) {
    }

    @Override
    public void travel(Vec3 p_20818_) {
        if (this.isInWater()) {
            this.moveRelative(0.02F, p_20818_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale((double)0.8F));
        } else if (this.isInLava()) {
            this.moveRelative(0.02F, p_20818_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
        } else {
            BlockPos ground = new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ());
            float f = 0.91F;
            if (this.onGround) {
                f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
            }

            float f1 = 0.16277137F / (f * f * f);
            f = 0.91F;
            if (this.onGround) {
                f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
            }

            this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, p_20818_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
        }

        this.calculateEntityAnimation(this, false);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.remainingLife--;
            if (this.remainingLife <= 0) {
                this.discard();
                return;
            }

            // 🛡️ 탈출 방지 해결: 소환사가 생존 중이고 거리가 24블록(거리 제곱 576D)을 초과하면 일방 복귀
            if (this.parentTurret != null && this.parentTurret.isAlive()) {
                double distanceToParentSqr = this.distanceToSqr(this.parentTurret);
                if (distanceToParentSqr > 576.0D) {
                    this.setTarget(null);
                    this.priorityDefenseTarget = null; // 보복 조준 초기화
                    this.getNavigation().moveTo(this.parentTurret, 1.25D); // 부모 포탑 좌표로 강제 복귀 비행
                    return; // 복귀 중에는 일반 탐색 AI 생략
                }
            }

            if (this.priorityDefenseTarget != null) {
                if (!this.priorityDefenseTarget.isAlive() || this.distanceToSqr(this.priorityDefenseTarget) > 1024.0D) {
                    this.priorityDefenseTarget = null;
                } else {
                    this.setTarget(this.priorityDefenseTarget);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            boolean hurtResult = target.hurt(DamageSource.mobAttack(this), this.attackDamage);
            if (hurtResult) {
                this.doEnchantDamageEffects(this, target);

                if (this.poisonLevel > 0) {
                    int duration = 40 + this.poisonLevel * 20;
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, duration, this.poisonLevel - 1));
                }

                if (this.witherLevel > 0) {
                    int duration = 40 + this.witherLevel * 20;
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, this.witherLevel - 1));
                }

                if (this.slownessLevel > 0) {
                    int duration = 40 + this.slownessLevel * 20;
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, this.slownessLevel - 1));
                }

                if (this.weaknessLevel > 0) {
                    int duration = 40 + this.weaknessLevel * 20;
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, this.weaknessLevel - 1));
                }
            }
            return hurtResult;
        }
        return false;
    }

    public static boolean isHostile(LivingEntity target) {
        if (target == null) return false;
        if (IAlly.isAllyEntity(target)) return false;
        return IZombieTribe.isZombieTribe(target) || target instanceof Monster;
    }

    public boolean isFlying() {
        return !this.onGround;
    }

    @Override
    public boolean isFlapping() {
        return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
    }

    public void setParentTurret(BeeSummonerTurretEntity turret) { this.parentTurret = turret; }
    public void setPriorityDefenseTarget(LivingEntity target) { this.priorityDefenseTarget = target; }

    public void setRemainingLife(int ticks) { this.remainingLife = ticks; }
    public void setAttackDamage(float damage) { this.attackDamage = damage; }

    public void setPoisonLevel(int level) { this.poisonLevel = level; }
    public void setWitherLevel(int level) { this.witherLevel = level; }
    public void setSlownessLevel(int level) { this.slownessLevel = level; }
    public void setWeaknessLevel(int level) { this.weaknessLevel = level; }

    public void setSearchRange(double range) {
        this.searchRange = range;
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(range);
    }
    public double getSearchRange() { return this.searchRange; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RemainingLife", this.remainingLife);
        tag.putFloat("AttackDamage", this.attackDamage);
        tag.putDouble("SearchRange", this.searchRange);

        tag.putInt("PoisonLevel", this.poisonLevel);
        tag.putInt("WitherLevel", this.witherLevel);
        tag.putInt("SlownessLevel", this.slownessLevel);
        tag.putInt("WeaknessLevel", this.weaknessLevel);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RemainingLife")) this.remainingLife = tag.getInt("RemainingLife");
        if (tag.contains("AttackDamage")) this.attackDamage = tag.getFloat("AttackDamage");
        if (tag.contains("SearchRange")) this.setSearchRange(tag.getDouble("SearchRange"));

        if (tag.contains("PoisonLevel")) this.poisonLevel = tag.getInt("PoisonLevel");
        if (tag.contains("WitherLevel")) this.witherLevel = tag.getInt("WitherLevel");
        if (tag.contains("SlownessLevel")) this.slownessLevel = tag.getInt("SlownessLevel");
        if (tag.contains("WeaknessLevel")) this.weaknessLevel = tag.getInt("WeaknessLevel");
    }

    private static class BeeRetaliateGoal extends HurtByTargetGoal {
        public BeeRetaliateGoal(PathfinderMob mob) {
            super(mob);
        }
        @Override
        public boolean canUse() {
            if (super.canUse()) {
                LivingEntity attacker = this.mob.getLastHurtByMob();
                return attacker != null && !IAlly.isAllyEntity(attacker);
            }
            return false;
        }
    }

    private static class BeeTargetHostileGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private final SummonedBeeEntity bee;
        public BeeTargetHostileGoal(SummonedBeeEntity bee) {
            super(bee, LivingEntity.class, 10, true, false, SummonedBeeEntity::isHostile);
            this.bee = bee;
        }
        @Override
        public boolean canUse() {
            this.targetConditions.range(this.bee.getSearchRange());
            return super.canUse();
        }
    }

    private class BeeWanderGoal extends Goal {
        public BeeWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return SummonedBeeEntity.this.navigation.isDone() && SummonedBeeEntity.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return SummonedBeeEntity.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 vec = this.findPos();
            if (vec != null) {
                SummonedBeeEntity.this.navigation.moveTo(SummonedBeeEntity.this.navigation.createPath(new BlockPos(vec), 1), 1.0D);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 view = SummonedBeeEntity.this.getViewVector(0.0F);
            Vec3 hover = HoverRandomPos.getPos(SummonedBeeEntity.this, 8, 7, view.x, view.z, ((float)Math.PI / 2F), 3, 1);
            return hover != null ? hover : AirAndWaterRandomPos.getPos(SummonedBeeEntity.this, 8, 4, -2, view.x, view.z, (double)((float)Math.PI / 2F));
        }
    }
}