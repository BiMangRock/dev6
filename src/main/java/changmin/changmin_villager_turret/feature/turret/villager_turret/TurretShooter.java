package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.config.TurretConfig;
import changmin.changmin_villager_turret.util.BallisticAim;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TurretShooter {

    // 독/위더 레벨을 맞은 몹에 기록하는 키 (예전 문자열 태그 "turret_poison_lvl_N" 대체)
    private static final String POISON_KEY = "changmin_turret_poison_lvl";
    private static final String WITHER_KEY = "changmin_turret_wither_lvl";

    // 동시 격발 모드들에 대한 타격 계산 [일반(0), 부채꼴(1), 강공격(4)]
    public static void shootTarget(VillagerTurretEntity turret, LivingEntity target) {
        int pattern = turret.getArrowPattern();
        int arrowCount = turret.getTurretArrowCount();
        Level level = turret.level();

        if (pattern == 4) { // 강공격 모드 (완전 동시 격발)
            for (int i = 0; i < arrowCount; i++) {
                shootSingleArrow(turret, target, 4, 0.0F);
            }
        } else if (pattern == 1) { // 부채꼴 사격 모드
            for (int i = 0; i < arrowCount; i++) {
                float baseSpread = (float) (14 - level.getDifficulty().getId() * 4);
                float angleOffset = (arrowCount > 1) ? (i - (arrowCount - 1) / 2.0F) * 4.0F : 0.0F;
                shootSingleArrow(turret, target, 1, baseSpread + angleOffset);
            }
        } else { // 기본 단발 모드 (0)
            shootSingleArrow(turret, target, 0, (float) (14 - level.getDifficulty().getId() * 4));
        }

        turret.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (turret.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // 단발 화살 격발 상세 연산
    public static void shootSingleArrow(VillagerTurretEntity turret, LivingEntity target, int mode, float spread) {
        TurretArrow arrow = new TurretArrow(turret.level(), turret);

        // 공격력 계산 (넉백 모드는 피해 1/10)
        double baseDamage = TurretConfig.arrowDamage() + turret.getDamageLevel() * TurretConfig.arrowDamagePerLevel();
        if (mode == 3) {
            baseDamage = baseDamage * 0.1D;
        }
        arrow.setBaseDamage(baseDamage);

        // 문자열 태그 대신 화살 필드에 직접 기록
        arrow.setShotMode(mode);
        arrow.setPassBlocks(turret.getPassBlocksEnabled() == 1);
        arrow.setPoisonLevel(turret.getPoisonLevel());
        arrow.setWitherLevel(turret.getWitherLevel());
        arrow.setSlownessLevel(turret.getSlownessLevel());
        arrow.setWeaknessLevel(turret.getWeaknessLevel());

        float finalSpeed = (float) (TurretConfig.arrowSpeed() + turret.getSpeedLevel() * TurretConfig.arrowSpeedPerLevel());
        boolean noGravity = turret.getNoGravityEnabled() == 1;
        arrow.setNoGravity(noGravity);

        // 🎯 탄도 계산: 적의 이동을 예측하고(리드샷), 중력 포물선을 정확히 풀어서 발사 방향 결정
        Vec3 dir = BallisticAim.solve(
                arrow.position(), target, 0.5D, finalSpeed,
                noGravity ? 0.0D : BallisticAim.ARROW_GRAVITY,
                BallisticAim.ARROW_DRAG,
                TurretConfig.leadTargeting());

        arrow.shoot(dir.x, dir.y, dir.z, finalSpeed, spread);
        turret.level().addFreshEntity(arrow);
    }

    // ==========================================
    // 💥 넉백·강공격 무적 소거, 상태이상 부여, 독/위더 피해 증폭
    // ==========================================
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        if (event.getSource().getDirectEntity() instanceof TurretArrow arrow) {
            if (arrow.resetsInvulnerability()) {
                victim.invulnerableTime = 0;
            }

            CompoundTag data = victim.getPersistentData();

            // 독/위더는 느린 1레벨 효과만 걸고, 실제 피해는 아래에서 레벨 비례로 증폭합니다. (서버 최적화)
            int poison = arrow.getPoisonLevel();
            if (poison > 0) {
                data.putInt(POISON_KEY, poison);
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, poison * 80, 0));
            }
            int wither = arrow.getWitherLevel();
            if (wither > 0) {
                data.putInt(WITHER_KEY, wither);
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, wither * 60, 0));
            }
            int slowness = arrow.getSlownessLevel();
            if (slowness > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowness * 100, slowness - 1));
            }
            int weakness = arrow.getWeaknessLevel();
            if (weakness > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weakness * 120, weakness - 1));
            }
        }

        // 🌟 독/위더 틱 피해를 레벨 비례로 증폭 (Method A)
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MAGIC) && victim.hasEffect(MobEffects.POISON)) {
            int level = victim.getPersistentData().getInt(POISON_KEY);
            if (level > 0) event.setAmount(event.getAmount() * level);
        }
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.WITHER) && victim.hasEffect(MobEffects.WITHER)) {
            int level = victim.getPersistentData().getInt(WITHER_KEY);
            if (level > 0) event.setAmount(event.getAmount() * level);
        }
    }

    // ==========================================
    // 🧱 벽 관통 화살: 블록에 막히면 반대편 빈 공간으로 워프
    // ==========================================
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof TurretArrow arrow) || !arrow.isPassBlocks()) return;

        HitResult result = event.getRayTraceResult();
        if (result.getType() != HitResult.Type.BLOCK) return;

        Level level = arrow.level();
        // 블록 충돌 취소. (Forge 1.20.1에서 setCanceled는 "제거 예정" 경고가 뜨지만 동작은 그대로입니다.
        //  권장 대체인 setImpactResult(SKIP_ENTITY)는 블록 충돌을 그대로 처리해 버려서 벽 관통이 깨지므로 쓰지 않습니다.)
        event.setCanceled(true);

        Vec3 hitLoc = result.getLocation();
        Vec3 motion = arrow.getDeltaMovement().normalize();

        Vec3 finalLoc = null;
        for (double warpDist = 0.5D; warpDist < 16.0D; warpDist += 0.5D) {
            Vec3 test = hitLoc.add(motion.scale(warpDist));
            BlockPos testPos = BlockPos.containing(test.x, test.y, test.z);
            if (level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty()) {
                finalLoc = test;
                break;
            }
        }
        if (finalLoc == null) return;

        // 1. 벽 속을 지나가는 구간에 있는 몹을 먼저 타격
        AABB pathBox = new AABB(hitLoc, finalLoc).inflate(0.5D);
        LivingEntity hitTarget = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, pathBox)) {
            if (entity != arrow.getOwner() && entity.isAlive()) {
                double dist = entity.distanceToSqr(hitLoc);
                if (dist < closestDist) {
                    closestDist = dist;
                    hitTarget = entity;
                }
            }
        }

        if (hitTarget != null) {
            Entity owner = arrow.getOwner();
            DamageSource damageSource = arrow.damageSources().arrow(arrow, owner == null ? arrow : owner);

            int knockback = arrow.getKnockback();
            if (knockback > 0) {
                Vec3 push = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(knockback * 0.6D);
                if (push.lengthSqr() > 0.0D) {
                    hitTarget.push(push.x, 0.1D, push.z);
                }
            }

            if (hitTarget.hurt(damageSource, (float) arrow.getBaseDamage())) {
                arrow.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (level.random.nextFloat() * 0.2F + 0.9F));
                arrow.discard();
            }
            return;
        }

        // 2. 관통 궤적 파티클
        if (level instanceof ServerLevel serverLevel) {
            double dist = hitLoc.distanceTo(finalLoc);
            int particleCount = Math.max(1, (int) (dist * 4));
            for (int i = 0; i <= particleCount; i++) {
                Vec3 p = hitLoc.add(finalLoc.subtract(hitLoc).scale((double) i / particleCount));
                serverLevel.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        arrow.teleportTo(finalLoc.x, finalLoc.y, finalLoc.z);
    }

    // 🗑️ 예전 onWorldTick(1초마다 월드 전체 엔티티 순회)은 삭제했습니다.
    //    TurretArrow.tick()에서 수명이 다하면 스스로 사라집니다.
}
