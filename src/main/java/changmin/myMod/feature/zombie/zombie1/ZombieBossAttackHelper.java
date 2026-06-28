package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;

public class ZombieBossAttackHelper {

    public static void handleAttackPatterns(ZombieBossEntity boss) {
        // 공격 1 선택문을 폐기하고 2, 3, 4 패턴에 집중합니다.
        if (boss.getActiveAttack() == 2) {
            handleAttack2(boss); // 정밀 조준 화살 사격 (기존 패턴 1)
        } else if (boss.getActiveAttack() == 3) {
            handleAttack3(boss); // 3D 구형/나선형 사격 (새 패턴 3)
        } else if (boss.getActiveAttack() == 4) {
            handleAttack4(boss); // 공중 360도 스핀 사격 (새 패턴 4)
        }
    }

    // ==========================================
    // 🎯 패턴 2: 정밀 조준 화살 발사 (기존 공격 1)
    // ==========================================
    private static void handleAttack2(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        LivingEntity target = boss.getTarget();

        int totalArrows = 5 + (boss.getBossLevel() - 1) * 10;
        int shootInterval = Math.max(1, 5 - (boss.getBossLevel() - 1));
        int maxTicks = totalArrows * shootInterval + 15;

        if (boss.getAttackTick() < maxTicks - 10) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.12D, boss.getDeltaMovement().z);
        } else {
            boss.setDeltaMovement(boss.getDeltaMovement().x, -0.05D, boss.getDeltaMovement().z);
        }

        if (target != null && boss.getAttackTick() % shootInterval == 0 && boss.getAttackTick() <= totalArrows * shootInterval) {
            shootSlowNoGravityArrow(boss, target);
        }

        if (boss.getAttackTick() >= maxTicks) {
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🌀 패턴 3: 3D 구형/나선형 상승 하강 사격
    // ==========================================
    private static void handleAttack3(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        if (currentTick <= 20) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.25D, boss.getDeltaMovement().z);
        } else if (currentTick <= 40) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, -0.2D, boss.getDeltaMovement().z);
        }

        double yaw = boss.getYRot() + (currentTick * 18.0D);
        double pitch = -60.0D + (Math.sin((currentTick * Math.PI) / 20.0D) * 60.0D);

        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(yawRad) * Math.cos(pitchRad);

        shootArrowInDirection(boss, dx, dy, dz, 0.35F, 2.5D);

        if (currentTick >= 40) {
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🌪️ 패턴 4: 공중 360도 회전 사격
    // ==========================================
    private static void handleAttack4(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        if (currentTick <= 25) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.3D, boss.getDeltaMovement().z);
        } else if (currentTick <= 55) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.0D, boss.getDeltaMovement().z);

            double yaw = boss.getYRot() + ((currentTick - 25) * 12.0D);
            double yawRad = Math.toRadians(yaw);

            double dx = -Math.sin(yawRad);
            double dy = 0.05D;
            double dz = Math.cos(yawRad);

            shootArrowInDirection(boss, dx, dy, dz, 0.6F, 3.0D);
        }

        if (currentTick >= 55) {
            spawnArrowRing(boss, 30);
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🛠️ 공통 발사 유틸리티 메서드
    // ==========================================

    private static void shootArrowInDirection(ZombieBossEntity boss, double dx, double dy, double dz, float speed, double damage) {
        ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
        arrow.setPos(boss.getX(), boss.getEyeY() - 0.1D, boss.getZ());
        arrow.shoot(dx, dy, dz, speed, 0.0F);
        arrow.setNoGravity(true);
        arrow.setBaseDamage(damage);
        boss.level.addFreshEntity(arrow);
    }

    private static void shootSlowNoGravityArrow(ZombieBossEntity boss, LivingEntity target) {
        ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
        arrow.setPos(boss.getX(), boss.getEyeY() - 0.1D, boss.getZ());

        double dx = target.getX() - boss.getX();
        double dy = target.getY(0.5D) - arrow.getY();
        double dz = target.getZ() - boss.getZ();

        arrow.shoot(dx, dy, dz, 0.4F, 0.0F);
        arrow.setNoGravity(true);
        arrow.setBaseDamage(2.0D);

        boss.level.addFreshEntity(arrow);
    }

    private static void spawnArrowRing(ZombieBossEntity boss, int arrowCount) {
        for (int i = 0; i < arrowCount; i++) {
            double angle = i * (2 * Math.PI / arrowCount);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
            arrow.setPos(boss.getX(), boss.getY() + 0.5D, boss.getZ());

            arrow.shoot(dx, 0.05D, dz, 0.7F, 0.0F);
            arrow.setBaseDamage(3.0D);

            boss.level.addFreshEntity(arrow);
        }
    }
}