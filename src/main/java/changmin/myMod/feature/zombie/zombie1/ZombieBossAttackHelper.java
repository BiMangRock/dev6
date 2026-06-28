package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;

public class ZombieBossAttackHelper {

    public static void handleAttackPatterns(ZombieBossEntity boss) {
        if (boss.getActiveAttack() == 1) {
            handleAttack1(boss);
        } else if (boss.getActiveAttack() == 3) {
            handleAttack3(boss);
        }
    }

    private static void handleAttack1(ZombieBossEntity boss) {
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

    private static void handleAttack3(ZombieBossEntity boss) {
        boss.setAttackTick(boss.getAttackTick() + 1);

        if (boss.getAttackTick() <= 15) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.35D, boss.getDeltaMovement().z);
        } else if (boss.getAttackTick() <= 25) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, 0.0D, boss.getBossLevel());
        } else if (boss.getAttackTick() <= 35) {
            boss.setDeltaMovement(boss.getDeltaMovement().x, -0.7D, boss.getDeltaMovement().z);
        } else if (boss.getAttackTick() == 36) {
            int arrowCount = 40 + (boss.getBossLevel() - 1) * 10;
            spawnArrowRing(boss, arrowCount);
            boss.setActiveAttack(0);
            boss.setAttackTick(0);
        }
    }

    // ==========================================
    // 🎯 정밀 조준 방식의 무중력 유도 화살 발사
    // ==========================================
    private static void shootSlowNoGravityArrow(ZombieBossEntity boss, LivingEntity target) {
        // 무한 비행 방지 전용 화살로 교체 [1.1.7]
        ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
        arrow.setPos(boss.getX(), boss.getEyeY() - 0.1D, boss.getZ());

        double dx = target.getX() - boss.getX();
        // 🆕 중력 보정용 수식을 지우고, 타겟의 정중앙(가슴 부분)을 레이저처럼 똑바로 응시하도록 수정 [2.2.3]
        double dy = target.getY(0.5D) - arrow.getY();
        double dz = target.getZ() - boss.getZ();

        // 🆕 마지막 인자인 탄퍼짐 계수를 0.0F로 변경하여 오차 각도 없이 조준선을 따라 정밀 조준 발사합니다 [2.2.1].
        arrow.shoot(dx, dy, dz, 0.4F, 0.0F);
        arrow.setNoGravity(true);
        arrow.setBaseDamage(2.0D);

        boss.level.addFreshEntity(arrow);
    }

    // ==========================================
    // 🌀 정교한 사방 링 모양의 화살 방출
    // ==========================================
    private static void spawnArrowRing(ZombieBossEntity boss, int arrowCount) {
        for (int i = 0; i < arrowCount; i++) {
            double angle = i * (2 * Math.PI / arrowCount);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            // 무한 비행 방지 전용 화살로 교체 [1.1.7]
            ZombieBossArrow arrow = new ZombieBossArrow(boss.level, boss);
            arrow.setPos(boss.getX(), boss.getY() + 0.5D, boss.getZ());

            // 탄퍼짐을 0.0F로 설정해 링 형태가 찌그러지지 않고 고르게 방출되도록 합니다 [2.2.1].
            arrow.shoot(dx, 0.05D, dz, 0.7F, 0.0F);
            arrow.setBaseDamage(3.0D);

            boss.level.addFreshEntity(arrow);
        }
    }
}