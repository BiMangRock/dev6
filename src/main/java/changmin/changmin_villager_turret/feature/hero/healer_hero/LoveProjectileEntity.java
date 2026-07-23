package changmin.changmin_villager_turret.feature.hero.healer_hero;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile; // 🆕 수정: Projectile 대신 ThrowableProjectile 사용
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class LoveProjectileEntity extends ThrowableProjectile implements IAnimatable { // 🆕 ThrowableProjectile 상속
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int lifeTicks = 0;
    private int maxLifeTicks = 200; // 초깃값 10초(200틱)
    private boolean hitBlock = false;

    public LoveProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public LoveProjectileEntity(Level level, LivingEntity owner, int heroLevel) {
        super(ModEntityTypes.LOVE_PROJECTILE.get(), owner, level); // 🆕 생명체 조준과 물리 루프 자동 연동
        this.maxLifeTicks = 200 + (heroLevel / 5) * 40;
    }

    // 🆕 무중력 상태로 직진 비행하기 위해 중력가속도를 0으로 오버라이드합니다.
    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        if (this.hitBlock) {
            // 벽에 피격되어 꽂힌 경우 물리 법칙 정지 후 제자리 고정
            this.setDeltaMovement(Vec3.ZERO);
            // 💡 주의: ThrowableProjectile.super.tick() 대신 Entity.super.tick()을 호출하여
            // 추가적인 물리 연산을 스킵하고, 시간 만료 타이머만 가동시킵니다.
            this.baseTick();
        } else {
            // 벽에 닿기 전에는 ThrowableProjectile의 내장 충돌 검사 및 비행 물리 틱을 그대로 수행합니다.
            super.tick();
        }

        this.lifeTicks++;

        if (!this.level.isClientSide) {
            if (this.lifeTicks >= this.maxLifeTicks) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!this.level.isClientSide && hit instanceof LivingEntity living) {
            if (IZombieTribe.isZombieTribe(living) && !(living instanceof IAlly)) {
                // 적 타격 시 피해 유발 (6.0 고정 대미지) 및 공격 경험치 누적
                if (this.getOwner() instanceof LivingEntity owner) {
                    living.hurt(DamageSource.indirectMobAttack(this, owner), 6.0F);
                    if (owner instanceof HealerHeroEntity hero) {
                        hero.recordAttackXP(1); // 공격 XP 1 부여
                    }
                } else {
                    living.hurt(DamageSource.GENERIC, 6.0F);
                }
                this.discard(); // 생명체 타격 시 소멸
            } else if (IAlly.isAllyEntity(living)) {
                // 아군 타격 시 치유 유발 (4.0 고정 회복) 및 힐 경험치 누적
                living.heal(4.0F);

                if (this.getOwner() instanceof HealerHeroEntity hero) {
                    hero.recordHealXP(1); // 아군 맞춤 시 힐 XP 1 부여
                }

                if (this.level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, living.getX(), living.getY() + 0.5D, living.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
                }
                this.discard(); // 아군 타격 시 소멸
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // 벽이나 지면에 피격 시 소멸하지 않고, 물리 판정을 정지
        this.hitBlock = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("MaxLifeTicks", this.maxLifeTicks);
        tag.putBoolean("HitBlock", this.hitBlock);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifeTicks = tag.getInt("LifeTicks");
        this.maxLifeTicks = tag.getInt("MaxLifeTicks");
        this.hitBlock = tag.getBoolean("HitBlock");
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().loop("play"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }
}