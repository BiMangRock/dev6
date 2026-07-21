package changmin.changmin_villager_turret.feature.zombie.assassin2;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class Assassin2AttackGoal extends Goal {
    private final Assassin2Entity assassin;
    private int attackCooldown = 0;

    public Assassin2AttackGoal(Assassin2Entity assassin) {
        this.assassin = assassin;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return assassin.getTarget() != null && assassin.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = assassin.getTarget();
        if (target == null) return;

        // 타겟의 상체 쪽을 바라보도록 설정
        assassin.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distSq = assassin.distanceToSqr(target);

        if (attackCooldown > 0) attackCooldown--;

        // 사거리 약 15블록 이내에서 검귀 발사
        if (distSq < 225.0D && attackCooldown <= 0) {
            shootSwordGhoul(target);
            // 레벨업 시 쿨타임 감소 (최소 15틱까지)
            this.attackCooldown = Math.max(15, 40 - (assassin.getAssassinLevel() * 2));
        }
    }

    private void shootSwordGhoul(LivingEntity target) {
        assassin.setAttacking(true);

        // 💡 [수정] 타겟의 눈 위치와 나의 눈 위치를 계산하여 3D 방향 벡터 추출
        Vec3 myPos = assassin.position().add(0, assassin.getEyeHeight(), 0);
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.8, 0);
        Vec3 dir = targetPos.subtract(myPos).normalize();

        double speed = 0.8D; // 날아가는 속도

        // 💡 [수정] dir.y를 포함시켜 위아래로도 조준 가능하게 함
        SwordGhoulEntity ghoul = new SwordGhoulEntity(assassin.level, assassin,
                dir.x * speed, dir.y * speed, dir.z * speed);

        assassin.level.addFreshEntity(ghoul);
        assassin.attackTimer = 10; // 애니메이션 타이머
    }
}