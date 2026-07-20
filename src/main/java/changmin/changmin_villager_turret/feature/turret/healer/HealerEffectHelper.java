package changmin.changmin_villager_turret.feature.turret.healer;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HealerEffectHelper {

    // 🆕 단일 줄기 빔 연출
    public static void spawnSingleHealLineEffects(ServerLevel level, HealerTurretEntity healer, LivingEntity target) {
        double startX = healer.getX();
        double startY = healer.getY(1.2D);
        double startZ = healer.getZ();

        double endX = target.getX();
        double endY = target.getY(0.5D);
        double endZ = target.getZ();

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int particleCount = (int) (distance * 4);
        for (int i = 0; i <= particleCount; i++) {
            double ratio = (double) i / particleCount;
            double px = startX + dx * ratio;
            double py = startY + dy * ratio;
            double pz = startZ + dz * ratio;

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        level.sendParticles(ParticleTypes.HEART, endX, endY + 0.5D, endZ, 5, 0.3D, 0.3D, 0.3D, 0.02D);
        healer.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.2F + healer.getRandom().nextFloat() * 0.3F);
    }

    // 🆕 광역 파동 고리(Ring) 연출
    public static void spawnAoeRingEffects(ServerLevel level, HealerTurretEntity healer) {
        double centerX = healer.getX();
        double centerY = healer.getY();
        double centerZ = healer.getZ();

        double maxRange = 8.0D + (healer.getRangeLevel() * 1.0D);
        double[] radii = { 2.0D, maxRange / 2.0D, maxRange };

        for (double r : radii) {
            int points = (int) (r * 8);
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i;
                double px = centerX + Math.cos(angle) * r;
                double pz = centerZ + Math.sin(angle) * r;

                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, centerY + 0.1D, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        healer.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.0F + healer.getRandom().nextFloat() * 0.2F);
    }

    // 개별 버블 연출
    public static void spawnHealBubbleEffects(ServerLevel level, LivingEntity entity) {
        level.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY(0.5D), entity.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.01D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY(0.5D), entity.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.01D);
    }

    // 주변 좀비 정화 피해 (어그로 반격 시스템 유도)
    public static void damageUndeadInRange(ServerLevel level, HealerTurretEntity healer) {
        double range = 8.0D + (healer.getRangeLevel() * 1.0D);
        AABB box = healer.getBoundingBox().inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity entity : targets) {
            if (entity.isAlive() && (entity.getMobType() == MobType.UNDEAD || IZombieTribe.isZombieTribe(entity))) {
                entity.hurt(DamageSource.mobAttack(healer), 1.0F); // 1 고정 피해 및 도발 유도

                // 🆕 피격 연출 (검은 연기 및 작은 불꽃)
                level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(0.5D), entity.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.01D);
                level.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(0.5D), entity.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.01D);
            }
        }
    }
}