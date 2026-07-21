package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelRangedAttackGoal;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.IAngelAttack;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.ally.IAlly;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class RagedAngelZombieEntity extends Monster implements IAnimatable, IZombieTribe, IAngelAttack {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    @Nullable private LivingEntity leader = null;
    public int attackTimer = 0;

    // 3연발 로직용 변수
    private int shotsLeft = 0;
    private int nextShotDelay = 0;
    private LivingEntity currentTarget = null;

    public RagedAngelZombieEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override public Monster asMonster() { return this; }
    public void setLeader(@Nullable LivingEntity leader) { this.leader = leader; }

    // 공격 시퀀스 시작 (AI에서 호출)
    @Override
    public void startAttackSequence(LivingEntity target) {
        this.shotsLeft = 3;
        this.currentTarget = target;
        this.nextShotDelay = 0; // 즉시 첫 발
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            // 3연발 로직 (10틱 = 0.5초 간격)
            if (this.shotsLeft > 0 && this.currentTarget != null && this.currentTarget.isAlive()) {
                if (this.nextShotDelay <= 0) {
                    shootRagedShockwave(this.currentTarget);
                    this.shotsLeft--;
                    this.nextShotDelay = 10;
                } else {
                    this.nextShotDelay--;
                }
            }
        }
        if (this.attackTimer > 0) this.attackTimer--;
    }

    private void shootRagedShockwave(LivingEntity target) {
        Vec3 dir = target.position().subtract(this.position()).normalize();
        double speed = 0.4D;
        RagedShockwaveEntity projectile = new RagedShockwaveEntity(this.level, this, dir.x * speed, dir.y * speed, dir.z * speed);
        this.level.addFreshEntity(projectile);
        this.attackTimer = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FLYING_SPEED, 0.7D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReturnToLeaderGoal(this));
        this.goalSelector.addGoal(2, new AngelRangedAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> entity instanceof IAlly));
    }

    // --- 주인 귀환 및 이동 로직 ---
    @Override public void travel(Vec3 p_21280_) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            this.moveRelative(0.02F, p_21280_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
        }
        this.calculateEntityAnimation(this, false);
    }
    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }

    static class ReturnToLeaderGoal extends Goal {
        private final RagedAngelZombieEntity angel;
        public ReturnToLeaderGoal(RagedAngelZombieEntity angel) { this.angel = angel; this.setFlags(EnumSet.of(Goal.Flag.MOVE)); }
        @Override public boolean canUse() { return angel.leader != null && angel.leader.isAlive() && angel.distanceToSqr(angel.leader) > 400.0D; }
        @Override public void tick() { if (angel.leader != null) angel.getNavigation().moveTo(angel.leader.getX(), angel.leader.getY() + 6, angel.leader.getZ(), 1.6D); }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.attackTimer > 0) event.getController().setAnimation(new AnimationBuilder().playOnce("attack1"));
        else event.getController().setAnimation(new AnimationBuilder().loop("fly"));
        return PlayState.CONTINUE;
    }
    @Override public void registerControllers(AnimationData data) { data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate)); }
    @Override public AnimationFactory getFactory() { return factory; }
}