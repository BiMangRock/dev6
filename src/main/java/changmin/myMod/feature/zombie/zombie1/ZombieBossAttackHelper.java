package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;

public class ZombieBossAttackHelper {

    public static void handleAttackPatterns(ZombieBossEntity boss) {
        if (boss.getActiveAttack() == 2) {
            handleAttack2(boss); // 정밀 조준 화살 사격 (기존 패턴 1)
        } else if (boss.getActiveAttack() == 3) {
            handleAttack3(boss); // 3D 구형/나선형 밀도 강화 사격 (수정됨)
        } else if (boss.getActiveAttack() == 5) {
            handleAttack5(boss); // 🆕 제자리 360도 진공 화살 난사 (신설)
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
    // 🌀 패턴 3: 3D 구형/나선형 사격 (화살 밀도 대폭 증가)
    // ==========================================
    private static void handleAttack3(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        if (currentTick <= 20) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.25D, boss.getDeltaMovement().z);
        } else if (currentTick <= 40) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, -0.2D, boss.getDeltaMovement().z);
        }

        // 기본 틱 회전 각도 계산
        double yaw = boss.getYRot() + (currentTick * 18.0D);
        double pitch = -60.0D + (Math.sin((currentTick * Math.PI) / 20.0D) * 60.0D);

        // 💡 [개선] 바라보고 있는 조준 축 기준 부채꼴(-12도, 0도, 12도)로 3방향 뿜칠 사격을 진행해 밀도를 높입니다 [1].
        double[] yawOffsets = {-12.0D, 0.0D, 12.0D};
        for (double offset : yawOffsets) {
            double totalYaw = yaw + offset;
            double pitchRad = Math.toRadians(pitch);
            double yawRad = Math.toRadians(totalYaw);

            double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
            double dy = -Math.sin(pitchRad);
            double dz = Math.cos(yawRad) * Math.cos(pitchRad);

            shootArrowInDirection(boss, dx, dy, dz, 0.35F, 2.5D);
        }

        if (currentTick >= 40) {
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🌪️ 패턴 5: 🆕 제자리 360도 진공 화살 난사 (신설)
    // ==========================================
    private static void handleAttack5(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);
        int currentTick = boss.getAttackTick();

        // 💡 제자리에서 발사하므로 X, Z 이동 성분만 차단하고 중력 보정은 기존처럼 유지합니다.
        boss.setDeltaMovement(0, boss.getDeltaMovement().y, 0);

        // 40틱(2초) 동안 작동하며 1틱당 18도씩 회전 (촘촘하게 총 2바퀴 회전)
        double yaw = boss.getYRot() + (currentTick * 18.0D);

        // 시각적으로 무거운 진공 장벽 효과를 주기 위해 수평 3방향 부채꼴 방향 설계
        double[] yawOffsets = {-15.0D, 0.0D, 15.0D};
        for (double offset : yawOffsets) {
            double totalYaw = yaw + offset;
            double pitchRad = Math.toRadians(5.0D); // 살짝 아래(5도)를 조준하도록 고정
            double yawRad = Math.toRadians(totalYaw);

            double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
            double dy = -Math.sin(pitchRad);
            double dz = Math.cos(yawRad) * Math.cos(pitchRad);

            // 💡 탄속을 0.2F로 매우 느리게 설정하여 '무겁게 머무는 진공 탄막 장벽'을 연출합니다.
            shootArrowInDirection(boss, dx, dy, dz, 0.2F, 2.0D);
        }

        if (currentTick >= 40) {
            // 패턴 종료 시 보너스로 사방 넓게 고속 링 방출
            spawnArrowRing(boss, 35);
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