package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.EnumSet;

public class AngelZombieEntity extends Monster implements IAnimatable, IZombieTribe {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int attackTimer = 0;

    public AngelZombieEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> entity instanceof IAlly));

        this.goalSelector.addGoal(1, new AngelRangedAttackGoal(this));
        this.goalSelector.addGoal(2, new AngelFlyWanderGoal(this));
    }

    // [패턴 1] 화살 공격
    public void performRangedAttack(LivingEntity target) {
        AngelZombieArrow arrow = new AngelZombieArrow(this.level, this);
        arrow.setNoGravity(true);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.shoot(dx, dy, dz, 1.8F, 0.0F);
        arrow.setBaseDamage(5.0D);
        this.level.addFreshEntity(arrow);
        this.attackTimer = 15;
    }

    public void performDonutAttack(LivingEntity target) {
        if (!this.level.isClientSide && target != null) {
            // 타겟 방향 계산
            Vec3 targetPos = target.position();
            Vec3 myPos = this.position();
            Vec3 direction = targetPos.subtract(myPos).normalize();

            // 이동 속도 설정 (0.3D 정도로 천천히 이동하며 퍼짐)
            double speed = 0.3D;
            ShockwaveEntity shockwave = new ShockwaveEntity(this.level, this,
                    direction.x * speed, direction.y * speed, direction.z * speed);

            this.level.addFreshEntity(shockwave);
            this.attackTimer = 15;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) this.attackTimer--;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.attackTimer > 0) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack1"));
        } else {
            event.getController().setAnimation(new AnimationBuilder().loop("fly"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public void travel(Vec3 p_21280_) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            float f = this.isInWater() ? 0.8F : 0.91F;
            this.moveRelative(0.02F, p_21280_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(f));
        }
        this.calculateEntityAnimation(this, false);
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource s) { return false; }
    @Override
    protected void checkFallDamage(double d, boolean b, BlockState s, BlockPos p) {}
    @Override
    protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override
    public AnimationFactory getFactory() { return factory; }

    static class AngelFlyWanderGoal extends Goal {
        private final AngelZombieEntity entity;
        public AngelFlyWanderGoal(AngelZombieEntity entity) { this.entity = entity; this.setFlags(EnumSet.of(Goal.Flag.MOVE)); }
        @Override
        public boolean canUse() { return !entity.getNavigation().isInProgress() && entity.getRandom().nextInt(10) == 0; }
        @Override
        public boolean canContinueToUse() { return entity.getNavigation().isInProgress(); }
        @Override
        public void start() {
            Vec3 viewVector = entity.getViewVector(0.0F);
            Vec3 vec3 = AirAndWaterRandomPos.getPos(entity, 8, 7, -1, viewVector.x, viewVector.z, (float)Math.PI / 2F);
            if (vec3 != null) entity.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0D);
        }
    }
}