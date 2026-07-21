package changmin.changmin_villager_turret.feature.zombie.assassin2;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.util.Mth;
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

public class SwordGhoulEntity extends Projectile implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int ageTicks = 0;

    public SwordGhoulEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SwordGhoulEntity(Level level, LivingEntity owner, double vx, double vy, double vz) {
        this(ModEntityTypes.SWORD_GHOUL.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.5D, owner.getZ());
        this.setDeltaMovement(vx, vy, vz);

        // 초기 회전값 동기화
        this.updateRotation();
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;

        // 위치 이동
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        // 💡 화살처럼 날아가는 방향을 바라보게 함
        this.updateRotation();

        if (!this.level.isClientSide) {
            // 시간이 지날수록 모델 크기에 맞춰 판정 범위 확장 (최대 4배)
            double scale = Math.min(4.0D, 1.0D + (ageTicks * 0.15D));
            AABB area = this.getBoundingBox().inflate(scale, 0.5D, scale);
            List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity target : targets) {
                if (target != this.getOwner() && !IZombieTribe.isZombieTribe(target)) {
                    target.hurt(DamageSource.mobAttack((LivingEntity) this.getOwner()), 6.0F);
                }
            }

            if (this.ageTicks > 40) {
                this.discard();
            }
        }
    }

    // 💡 [수정됨] 부모 클래스인 Projectile의 updateRotation을 오버라이드함
    // private을 protected로 바꾸고 @Override를 추가했습니다.
    @Override
    protected void updateRotation() {
        Vec3 movement = this.getDeltaMovement();
        double horizontalDist = movement.horizontalDistance();

        // 날아가는 벡터를 기반으로 각도(Yaw, Pitch) 계산
        this.setYRot((float)(Mth.atan2(movement.x, movement.z) * (180D / Math.PI)));
        this.setXRot((float)(Mth.atan2(movement.y, horizontalDist) * (180D / Math.PI)));

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override protected void defineSynchedData() {}

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().loop("fly"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}