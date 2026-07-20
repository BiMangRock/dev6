package changmin.changmin_villager_turret.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;

public class ZombieBossAttackHelper {

    public static void handleAttackPatterns(ZombieBossEntity boss) {
        if (boss.getActiveAttack() == 2) {
            handleAttack2(boss);
        } else if (boss.getActiveAttack() == 3) {
            handleAttack3(boss); // 💡 [수정] 수직 이동 수평 사방 원기둥 난사 패턴
        } else if (boss.getActiveAttack() == 5) {
            handleAttack5(boss);
        }
    }

    // 광폭화 패시브 기믹: 체력 50% 이하일 때 2틱(0.1초)마다 꼬리물기 추적 사격 수행 [1]
    public static void handlePassiveGimmick(ZombieBossEntity boss) {
        if (boss.getHealth() <= boss.getMaxHealth() * 0.5F) {
            LivingEntity target = boss.getTarget();
            if (target != null && target.isAlive() && boss.tickCount % 2 == 0) {
                shootPassiveTrackerArrow(boss, target);
            }
        }
    }

    private static void shootPassiveTrackerArrow(ZombieBossEntity boss, LivingEntity target) {
        ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
        arrow.setPos(boss.getX(), boss.getEyeY() - 0.1D, boss.getZ());

        double dx = target.getX() - boss.getX();
        double dy = target.getY(0.5D) - arrow.getY();
        double dz = target.getZ() - boss.getZ();

        arrow.shoot(dx, dy, dz, 0.25F, 0.0F);
        arrow.setNoGravity(true);
        arrow.setBaseDamage(2.0D);

        arrow.setMaxLifeTicks(300); // 패시브 유도 화살만 수명을 15초(300틱)로 연장 [2]

        boss.level.addFreshEntity(arrow);
    }

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
    // 🌀 패턴 3: [재설계] 수직 이동형 원기둥 탄막 난사 패턴 [1]
    // ==========================================
    private static void handleAttack3(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        // 수직 상승 및 하강 모션 제어
        if (currentTick <= 20) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.25D, boss.getDeltaMovement().z);
        } else if (currentTick <= 40) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, -0.2D, boss.getDeltaMovement().z);
        }

        // 💡 [수정] 3차원 회전을 삭제하고, 상승/하강하는 동안 매 틱마다 보스 수평 사방(12방향 원형)으로 화살 방출 [1]
        // 보스가 움직이며 고리형 탄막을 스폰하므로 하늘 방향으로 거대한 원기둥형 화살 장벽이 연출됩니다.
        spawnContinuousArrowRing(boss, 12);

        if (currentTick >= 40) {
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🌪️ 패턴 5: 제자리 360도 진공 화살 난사
    // ==========================================
    private static void handleAttack5(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        boss.setDeltaMovement(0, boss.getDeltaMovement().y, 0);

        double yaw = boss.getYRot() + (currentTick * 18.0D);

        double[] yawOffsets = {-15.0D, 0.0D, 15.0D};
        for (double offset : yawOffsets) {
            double totalYaw = yaw + offset;
            double pitchRad = Math.toRadians(5.0D);
            double yawRad = Math.toRadians(totalYaw);

            double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
            double dy = -Math.sin(pitchRad);
            double dz = Math.cos(yawRad) * Math.cos(pitchRad);

            shootArrowInDirection(boss, dx, dy, dz, 0.2F, 2.0D);
        }

        if (currentTick >= 40) {
            spawnArrowRing(boss, 35);
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🛠️ 공통 발사 유틸리티 메서드
    // ==========================================

    // 💡 [추가] 수직 이동 중에 완전히 평평한 수평 원형 탄막을 쏘기 위한 전용 링 사격 보조 메서드 [1]
    private static void spawnContinuousArrowRing(ZombieBossEntity boss, int arrowCount) {
        for (int i = 0; i < arrowCount; i++) {
            double angle = i * (2 * Math.PI / arrowCount);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            // dy 성분을 0.0D로 강제 고정하여 기울기 없이 수평으로만 퍼지게 발사합니다.
            shootArrowInDirection(boss, dx, 0.0D, dz, 0.35F, 2.5D);
        }
    }

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