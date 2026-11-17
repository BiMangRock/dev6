package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RagedShockwaveEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
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

        if (!this.level().isClientSide) {
            ageTicks++;
            double currentRadius = (double) ageTicks / MAX_AGE * 18.0D;
            if (ageTicks % 5 == 0) this.checkAndDamage(currentRadius);
            if (ageTicks >= MAX_AGE) this.discard();
        }
    }

    private void checkAndDamage(double radius) {
        AABB area = this.getBoundingBox().inflate(radius, 2.0D, radius);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : targets) {
            if (target == this.getOwner() || IZombieTribe.isZombieTribe(target)) continue;
            double dist = this.distanceTo(target);
            if (dist > radius - 2.0D && dist < radius + 2.0D) {
                // 💡 데미지 2.0 고정
                target.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), 2.0F);

                Vec3 pushDir = target.position().subtract(this.position()).normalize();
                target.push(pushDir.x * 0.4, 0.1, pushDir.z * 0.4);
            }
        }
    }

    @Override protected void defineSynchedData() {}
    private PlayState predicate(AnimationState<RagedShockwaveEntity> event) {
        event.getController().setAnimation(RawAnimation.begin().thenPlay("play"));
        return PlayState.CONTINUE;
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}