package changmin.myMod.feature.turret.plasma_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PlasmaOrbEntity extends AbstractArrow implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // 시각적 크기 동기화를 위한 데이터 패킷 구성
    private static final EntityDataAccessor<Float> ORB_SCALE = SynchedEntityData.defineId(PlasmaOrbEntity.class, EntityDataSerializers.FLOAT);

    private float damage = 5.0F;
    private int stunDuration = 100; // 기본 5초(100틱)

    public PlasmaOrbEntity(EntityType<PlasmaOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public PlasmaOrbEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.PLASMA_ORB.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ORB_SCALE, 1.0F);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.AIR);
    }

    @Override
    public void tick() {
        Vec3 motion = this.getDeltaMovement();
        super.tick();

        if (!this.level.isClientSide) {
            // 💡 무중력 상태에서 10초(200틱)가 경과하면 탄환을 소멸시켜 서버 과부하 방지
            if (this.tickCount >= 200) {
                this.discard();
                return;
            }
        }

        if (!this.inGround) {
            // 진공 상태 물리 법칙 적용
            this.setDeltaMovement(motion);
        }
    }

    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (!this.level.isClientSide) {
            Entity owner = this.getOwner();
            if (target instanceof LivingEntity livingTarget) {
                if (owner instanceof IAlly && livingTarget instanceof IAlly) {
                    this.discard();
                    return;
                }

                // 1. 공격자 터렛의 강화 수치에 기반한 데미지 가함
                livingTarget.hurt(DamageSource.indirectMagic(this, owner == null ? this : owner), this.damage);

                // 2. 공격자 터렛의 강화 수치에 기반한 기절 상태이상 부여
                livingTarget.addEffect(new MobEffectInstance(ModEffects.STUN.get(), this.stunDuration, 0));

                // 3. 타격 시 터렛에게 성장 경험치(XP) 주입
                if (owner instanceof PlasmaWizardEntity wizard) {
                    wizard.addXp(4); // 맞출 때마다 4 XP 획득
                }

                this.playSound(net.minecraft.sounds.SoundEvents.TRIDENT_HIT, 1.0F, 1.2F);
            }
            this.discard();
        }
    }

    public float getOrbScale() {
        return this.entityData.get(ORB_SCALE);
    }

    public void setOrbScale(float scale) {
        this.entityData.set(ORB_SCALE, scale);
        // 💡 시각적 크기와 동등하게 충돌 판정 히트박스(AABB) 크기도 가로세로 비례하여 실시간 확장
        double halfWidth = (0.5D * scale) / 2.0D;
        this.setBoundingBox(new AABB(
                this.getX() - halfWidth, this.getY() - halfWidth, this.getZ() - halfWidth,
                this.getX() + halfWidth, this.getY() + halfWidth, this.getZ() + halfWidth
        ));
    }

    public void setDamage(float dmg) { this.damage = dmg; }
    public void setStunDuration(int ticks) { this.stunDuration = ticks; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("OrbScale", this.getOrbScale());
        tag.putFloat("OrbDamage", this.damage);
        tag.putInt("OrbStunDuration", this.stunDuration);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OrbScale")) this.setOrbScale(tag.getFloat("OrbScale"));
        if (tag.contains("OrbDamage")) this.damage = tag.getFloat("OrbDamage");
        if (tag.contains("OrbStunDuration")) this.stunDuration = tag.getInt("OrbStunDuration");
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("fly", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}