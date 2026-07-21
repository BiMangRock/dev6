package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
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

public class ShockwaveEntity extends Projectile implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int ageTicks = 0;
    private static final int MAX_AGE = 80; // 애니메이션 4초 = 80틱

    public ShockwaveEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ShockwaveEntity(Level level, LivingEntity owner, double vx, double vy, double vz) {
        this(ModEntityTypes.SHOCKWAVE.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + 0.5D, owner.getZ());
        this.setDeltaMovement(vx, vy, vz); // 투사체 속도 설정
    }

    @Override
    public void tick() {
        super.tick();

        // 위치 업데이트 (투사체 이동)
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        if (!this.level.isClientSide) {
            ageTicks++;

            // 시간에 따라 반지름이 0에서 18까지 커짐 (애니메이션 Scale과 동기화)
            double currentRadius = (double) ageTicks / MAX_AGE * 18.0D;

            // 매 틱마다 혹은 일정 간격마다 판정 수행
            if (ageTicks % 5 == 0) {
                this.checkAndDamage(currentRadius);
            }

            if (ageTicks >= MAX_AGE) {
                this.discard();
            }
        }
    }

    private void checkAndDamage(double radius) {
        // 판정 범위 설정 (반지름에 따른 AABB 확장)
        AABB area = this.getBoundingBox().inflate(radius, 2.0D, radius);
        List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : targets) {
            // 1. 소환사 본인 제외
            if (target == this.getOwner()) continue;

            // 2. IZombieTribe(같은 편)인지 확인하여 제외 (수정된 핵심 로직)
            if (IZombieTribe.isZombieTribe(target)) continue;

            double dist = this.distanceTo(target);
            // 도넛의 테두리 부분(반경 +- 2.0블록)에 있는지 확인
            if (dist > radius - 2.0D && dist < radius + 2.0D) {
                // 데미지 입히기
                target.hurt(DamageSource.mobAttack((LivingEntity) this.getOwner()), 1.0F);

                // 넉백 (중심에서 바깥쪽으로)
                double dX = target.getX() - this.getX();
                double dZ = target.getZ() - this.getZ();
                double distance = Math.sqrt(dX * dX + dZ * dZ);
                if (distance > 0) {
                    target.push((dX / distance) * 0.5, 0.1, (dZ / distance) * 0.5);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {}

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().playOnce("play"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return factory; }
}