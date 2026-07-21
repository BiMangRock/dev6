package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class RagedShockwaveEntity extends Projectile implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int ageTicks = 0;
    private static final int MAX_AGE = 80;

    public RagedShockwaveEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public RagedShockwaveEntity(Level level, LivingEntity owner, double vx, double vy, double vz) {
        this(ModEntityTypes.RAGED_SHOCKWAVE.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + 0.5D, owner.getZ());
        this.setDeltaMovement(vx, vy, vz);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        if (!this.level.isClientSide) {
            ageTicks++;
            double currentRadius = (double) ageTicks / MAX_AGE * 18.0D;
            if (ageTicks % 5 == 0) this.checkAndDamage(currentRadius);
            if (ageTicks >= MAX_AGE) this.discard();
        }
    }

    private void checkAndDamage(double radius) {
        AABB area = this.getBoundingBox().inflate(radius, 2.0D, radius);
        List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : targets) {
            if (target == this.getOwner() || IZombieTribe.isZombieTribe(target)) continue;
            double dist = this.distanceTo(target);
            if (dist > radius - 2.0D && dist < radius + 2.0D) {
                // 💡 데미지 2.0 고정
                target.hurt(DamageSource.mobAttack((LivingEntity) this.getOwner()), 2.0F);

                Vec3 pushDir = target.position().subtract(this.position()).normalize();
                target.push(pushDir.x * 0.4, 0.1, pushDir.z * 0.4);
            }
        }
    }

    @Override protected void defineSynchedData() {}
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().playOnce("play"));
        return PlayState.CONTINUE;
    }
    @Override public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    @Override public AnimationFactory getFactory() { return factory; }
}