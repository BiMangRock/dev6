package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TurretShooter {

    // 동시 격발 모드들에 대한 타격 계산 [일반(0), 부채꼴(1), 강공격(4)]
    public static void shootTarget(VillagerTurretEntity turret, LivingEntity target) {
        int pattern = turret.getArrowPattern();
        int arrowCount = turret.getTurretArrowCount(); // 고유 변경 메소드 적용 완료
        Level level = turret.level;

        if (pattern == 4) { // 강공격 모드 (완전 동시 격발)
            for (int i = 0; i < arrowCount; i++) {
                shootSingleArrow(turret, target, 4, 0.0F); // 모드 4 부여
            }
        } else if (pattern == 1) { // 부채꼴 사격 모드
            for (int i = 0; i < arrowCount; i++) {
                float baseSpread = (float)(14 - level.getDifficulty().getId() * 4);
                float angleOffset = (arrowCount > 1) ? (i - (arrowCount - 1) / 2.0F) * 4.0F : 0.0F;
                shootSingleArrow(turret, target, 1, baseSpread + angleOffset); // 모드 1 부여
            }
        } else { // 기본 단발 모드 (0)
            shootSingleArrow(turret, target, 0, (float)(14 - level.getDifficulty().getId() * 4));
        }

        turret.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (turret.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // 단발 화살 격발 상세 연산
    public static void shootSingleArrow(VillagerTurretEntity turret, LivingEntity target, int mode, float spread) {
        // 토글형 물약 화살을 전량 폐기하고, 다중 상태이상 업그레이드를 태그로 유연 전달하도록 변경
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(turret, arrowStack, 1.0F);

        // 공격력 정밀 계산
        double baseDamage = VillagerTurretEntity.ARROW_DAMAGE + (turret.getDamageLevel() * 1.0D);
        if (mode == 3) { // 넉백 모드일 시 정수 나눗셈 방지를 위해 실수형(0.1D) 곱셈으로 데미지 격감 처리 (하트 0.1칸 수준)
            baseDamage = baseDamage * 0.1D;
        }
        arrow.setBaseDamage(baseDamage);

        // 기본 터렛 소속 태그 지정
        arrow.addTag("turret_arrow");

        if (turret.getPassBlocksEnabled() == 1) {
            arrow.addTag("pass_blocks");
        }

        // ==========================================
        // 🛡️ 무적 프레임 제어를 위한 충돌 분석용 태그 추가
        // ==========================================
        if (mode == 3) {
            arrow.addTag("knockback_arrow");
        } else if (mode == 4) {
            arrow.addTag("heavy_arrow");
        }

        // 해금 및 주입되어 있는 4종류의 독/위더/슬로우/나약함 레벨 수치를 화살 태그에 고스란히 담아 사격
        if (turret.getPoisonLevel() > 0) arrow.addTag("poison_" + turret.getPoisonLevel());
        if (turret.getWitherLevel() > 0) arrow.addTag("wither_" + turret.getWitherLevel());
        if (turret.getSlownessLevel() > 0) arrow.addTag("slowness_" + turret.getSlownessLevel());
        if (turret.getWeaknessLevel() > 0) arrow.addTag("weakness_" + turret.getWeaknessLevel());

        double d0 = target.getX() - turret.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - turret.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        float finalSpeed = VillagerTurretEntity.ARROW_SPEED + (turret.getSpeedLevel() * 0.15F);

        if (turret.getNoGravityEnabled() == 1) {
            arrow.setNoGravity(true);
            arrow.shoot(d0, d1, d2, finalSpeed, spread);
        } else {
            arrow.setNoGravity(false);
            arrow.shoot(d0, d1 + d3 * 0.2D, d2, finalSpeed, spread);
        }

        turret.level.addFreshEntity(arrow);
    }

    // ==========================================
    // 💥 넉백, 강공격 무적 소거 및 독/위더 데미지 다이내믹 수치 가로채기(Method A) 적용 연산
    // ==========================================
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // getImmediateSource() 대신 환경에 맞춘 getDirectEntity() 사용
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            LivingEntity victim = event.getEntityLiving();

            // 맞춘 화살이 넉백 모드 혹은 강공격 모드 전용 화살로 검출될 경우
            if (arrow.getTags().contains("knockback_arrow") || arrow.getTags().contains("heavy_arrow")) {
                // hurtResistantTime 대신 1.18.2의 실질 변수인 invulnerableTime을 0으로 강제 소거합니다.
                victim.invulnerableTime = 0;
            }

            // 태그에서 수치 정보를 꺼내어 효과 주입 (독/위더는 서버 최적화를 위해 느린 1레벨로 걸고 레벨 태그를 몹에게 심음)
            if (arrow.getTags().contains("turret_arrow")) {
                for (String tag : arrow.getTags()) {
                    if (tag.startsWith("poison_")) {
                        try {
                            int level = Integer.parseInt(tag.substring(7));
                            if (level > 0) {
                                victim.getTags().removeIf(t -> t.startsWith("turret_poison_lvl_"));
                                victim.addTag("turret_poison_lvl_" + level);
                                // 최적화를 위해 매 틱 데미지 주입이 아닌 일반 느린 독(독 1레벨) 주입
                                victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.POISON, level * 80, 0));
                            }
                        } catch (NumberFormatException ignored) {}
                    } else if (tag.startsWith("wither_")) {
                        try {
                            int level = Integer.parseInt(tag.substring(7));
                            if (level > 0) {
                                victim.getTags().removeIf(t -> t.startsWith("turret_wither_lvl_"));
                                victim.addTag("turret_wither_lvl_" + level);
                                // 최적화를 위해 느린 일반 위더(위더 1레벨) 주입
                                victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.WITHER, level * 60, 0));
                            }
                        } catch (NumberFormatException ignored) {}
                    } else if (tag.startsWith("slowness_")) {
                        try {
                            int level = Integer.parseInt(tag.substring(9));
                            if (level > 0) {
                                // 구속은 최대 5레벨 스펙에 맞춤 (표준 바닐라 적용)
                                victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, level * 100, level - 1));
                            }
                        } catch (NumberFormatException ignored) {}
                    } else if (tag.startsWith("weakness_")) {
                        try {
                            int level = Integer.parseInt(tag.substring(9));
                            if (level > 0) {
                                // 나약함은 최대 5레벨 스펙에 맞춤 (표준 바닐라 적용)
                                victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.WEAKNESS, level * 120, level - 1));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        // 🌟 [최적화 적용] 독/위더의 틱 피해 가로채기 연산 (Method A)
        LivingEntity victim = event.getEntityLiving();

        // 독 데미지는 DamageSource.MAGIC 이며 독 효과가 활성화되어 있을 때 적용
        if (event.getSource() == DamageSource.MAGIC && victim.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
            for (String tag : victim.getTags()) {
                if (tag.startsWith("turret_poison_lvl_")) {
                    try {
                        int level = Integer.parseInt(tag.substring(18));
                        if (level > 0) {
                            // 기본 1단계 독 데미지를 독 레벨 비례 뻥튀기
                            float basePoisonDmg = event.getAmount();
                            event.setAmount(basePoisonDmg * level);
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }

        // 위더 데미지는 DamageSource.WITHER 이며 위더 효과가 활성화되어 있을 때 적용
        if (event.getSource() == DamageSource.WITHER && victim.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
            for (String tag : victim.getTags()) {
                if (tag.startsWith("turret_wither_lvl_")) {
                    try {
                        int level = Integer.parseInt(tag.substring(18));
                        if (level > 0) {
                            // 기본 1단계 위더 데미지를 위더 레벨 비례 뻥튀기
                            float baseWitherDmg = event.getAmount();
                            event.setAmount(baseWitherDmg * level);
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            Level level = arrow.level;

            if (arrow.getTags().contains("turret_arrow")) {
                HitResult result = event.getRayTraceResult();
                boolean hasPassTag = arrow.getTags().contains("pass_blocks");

                if (hasPassTag) {
                    if (result.getType() == HitResult.Type.BLOCK) {
                        event.setCanceled(true); // 물리학 충돌 상쇄

                        Vec3 hitLoc = result.getLocation();
                        Vec3 motion = arrow.getDeltaMovement().normalize();

                        double warpDist = 0.5;
                        boolean foundOpenSpace = false;
                        double finalX = hitLoc.x;
                        double finalY = hitLoc.y;
                        double finalZ = hitLoc.z;

                        while (warpDist < 16.0) {
                            double testX = hitLoc.x + motion.x * warpDist;
                            double testY = hitLoc.y + motion.y * warpDist;
                            double testZ = hitLoc.z + motion.z * warpDist;
                            net.minecraft.core.BlockPos testPos = new net.minecraft.core.BlockPos(testX, testY, testZ);

                            if (level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty()) {
                                finalX = testX;
                                finalY = testY;
                                finalZ = testZ;
                                foundOpenSpace = true;
                                break;
                            }
                            warpDist += 0.5;
                        }

                        if (foundOpenSpace) {
                            Vec3 finalLoc = new Vec3(finalX, finalY, finalZ);

                            // 1. 궤적 간 몹 타격 가로채기 알고리즘
                            AABB pathBox = new AABB(hitLoc, finalLoc).inflate(0.5);
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
                                DamageSource damageSource = owner == null ? DamageSource.arrow(arrow, arrow) : DamageSource.arrow(arrow, owner);

                                int knockback = arrow.getKnockback();
                                if (knockback > 0) {
                                    Vec3 vec3 = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)knockback * 0.6D);
                                    if (vec3.lengthSqr() > 0.0D) {
                                        hitTarget.push(vec3.x, 0.1D, vec3.z);
                                    }
                                }

                                if (hitTarget.hurt(damageSource, (float)arrow.getBaseDamage())) {
                                    arrow.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (arrow.level.random.nextFloat() * 0.2F + 0.9F));
                                    arrow.discard();
                                }
                                return;
                            }

                            // 2. 관통 워프 공백 궤적 파티클 트레일 선명화
                            if (level instanceof ServerLevel serverLevel) {
                                double dist = hitLoc.distanceTo(finalLoc);
                                int particleCount = (int) (dist * 4);
                                for (int i = 0; i <= particleCount; i++) {
                                    double t = (double) i / particleCount;
                                    double px = hitLoc.x + (finalLoc.x - hitLoc.x) * t;
                                    double py = hitLoc.y + (finalLoc.y - hitLoc.y) * t;
                                    double pz = hitLoc.z + (finalLoc.z - hitLoc.z) * t;
                                    serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                                }
                            }

                            arrow.teleportTo(finalX, finalY, finalZ);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameTime() % 20 == 0) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof AbstractArrow arrow) {
                        if (arrow.getTags().contains("turret_arrow") && arrow.tickCount > 200) {
                            arrow.discard();
                        }
                    }
                }
            }
        }
    }
}