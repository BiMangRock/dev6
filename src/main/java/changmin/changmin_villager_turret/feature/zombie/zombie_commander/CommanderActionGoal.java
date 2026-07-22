package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.List;

public class CommanderActionGoal extends Goal {
    private final ZombieCommanderEntity commander;
    private int ultimateTimer = 0;
    private int normalActionTimer = 0;

    public CommanderActionGoal(ZombieCommanderEntity commander) {
        this.commander = commander;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        if (commander.level.isClientSide) return;

        // 1. 궁극기 쿨타임 (30초)
        if (ultimateTimer < 600) {
            ultimateTimer++;
            commander.setSyncCooldown(ultimateTimer);
        }

        // 2. 일반 행동 (2초마다 체크)
        normalActionTimer++;
        if (normalActionTimer >= 40) {
            normalActionTimer = 0;
            handleNormalActions();
        }

        // 3. 궁극기 발동
        if (ultimateTimer >= 600) {
            performUltimate();
            ultimateTimer = 0;
        }
    }

    private void handleNormalActions() {
        List<LivingEntity> allies = commander.level.getEntitiesOfClass(LivingEntity.class,
                commander.getBoundingBox().inflate(commander.getBuffRange()),
                e -> IZombieTribe.isZombieTribe(e) && !e.getUUID().equals(commander.getUUID()));

        List<LivingEntity> enemies = commander.level.getEntitiesOfClass(LivingEntity.class,
                commander.getBoundingBox().inflate(15.0D), e -> e instanceof IAlly && e.isAlive());

        if (allies.isEmpty()) {
            if (!enemies.isEmpty()) performAttack(enemies.get(0));
        } else {
            if (!enemies.isEmpty() && commander.getRandom().nextBoolean()) performAttack(enemies.get(0));
            else performBuff(allies);
        }
    }

    private void performBuff(List<LivingEntity> allies) {
        commander.triggerBuffAnimation();
        for (LivingEntity ally : allies) ZombieCommanderBuffHelper.applyHealthBuff(commander, ally);
        commander.recordBuffs(allies.size());
    }

    private void performAttack(LivingEntity target) {
        commander.triggerAttackAnimation();
        // 일반 공격: 타겟을 향해 발사
        int count = 2 + (commander.getBossLevel() / 5);
        for (int i = 0; i < count; i++) {
            ZombieCommanderProjectile proj = new ZombieCommanderProjectile(commander.level, commander);
            proj.setMaggotStats(commander.getMaggotAtk(), commander.getMaggotLife(), commander.getMaggotHp());
            double d0 = target.getX() - commander.getX();
            double d1 = target.getY(0.33D) - proj.getY();
            double d2 = target.getZ() - commander.getZ();
            proj.shoot(d0, d1, d2, 1.1F, (float)(4.0F + i));
            commander.level.addFreshEntity(proj);
        }
    }

    // 🌀 궁극기: 360도 원형 발사 로직
    private void performUltimate() {
        commander.triggerUltimateAnimation();

        // 궁극기 구더기 수: 기본 10마리 + 10레벨당 10마리 추가
        int count = 10 + (commander.getBossLevel() / 10) * 10;

        for (int i = 0; i < count; i++) {
            // 360도(2 * PI)를 구더기 수만큼 나누어 각도를 계산합니다.
            double angle = i * (2 * Math.PI / count);

            // 삼각함수를 이용해 X, Z축 발사 방향을 결정합니다.
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double dy = 0.2D; // 살짝 위로 포물선을 그리며 퍼지게 설정 (높이 유지)

            ZombieCommanderProjectile proj = new ZombieCommanderProjectile(commander.level, commander);
            proj.setMaggotStats(commander.getMaggotAtk(), commander.getMaggotLife(), commander.getMaggotHp());

            // 계산된 방향(dx, dy, dz)으로 발사합니다.
            // 속도는 0.8F로 설정하여 너무 멀리 퍼지지 않고 근처를 포위하게 했습니다.
            proj.shoot(dx, dy, dz, 0.8F, 0.0F);
            commander.level.addFreshEntity(proj);
        }
    }
}