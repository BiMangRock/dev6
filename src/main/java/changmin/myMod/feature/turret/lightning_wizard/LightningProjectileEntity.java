package changmin.myMod.feature.turret.lightning_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEffects;
import changmin.myMod.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class LightningProjectileEntity extends AbstractArrow {

    public LightningProjectileEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public LightningProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.LIGHTNING_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true); // 마법 탄환처럼 일직선 비행
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.AIR); // 화살 아이템 주울 수 없음
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            // 푸른색 마법 입자 꼬리를 실시간으로 생성해 마법탄 비주얼 구현
            this.level.addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (!this.level.isClientSide) {
            Entity owner = this.getOwner();
            if (target instanceof LivingEntity livingTarget) {
                // 아군 오사 방지
                if (owner instanceof IAlly && livingTarget instanceof IAlly) {
                    this.discard();
                    return;
                }

                // 피해 가하기
                livingTarget.hurt(DamageSource.indirectMagic(this, owner == null ? this : owner), 6.0F);

                // 마법사 정보 기반 기절 시간 및 궁극기 쿨타임 단축 적용
                int stunDuration = 40; // 기본 2초
                if (owner instanceof LightningWizardEntity wizard) {
                    stunDuration += wizard.getStunDurationLevel() * 10; // 레벨당 0.5초 증가
                    wizard.reduceUltCooldown(60); // 적중 성공 시 궁극기 대기 시간 3초 감소!
                }

                // 기절 포션 효과 주입
                livingTarget.addEffect(new MobEffectInstance(ModEffects.STUN.get(), stunDuration));

                // 적중 이펙트 터뜨리기
                if (this.level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH, livingTarget.getX(), livingTarget.getY(0.5D), livingTarget.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, livingTarget.getX(), livingTarget.getY(0.5D), livingTarget.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.1D);
                }
                this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.2F);
            }
            this.discard();
        }
    }
}