package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.config.TurretConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

/**
 * 🏹 주민 터렛 전용 화살
 *
 * 예전에는 "turret_arrow", "poison_3" 같은 문자열 태그를 화살에 붙이고 맞을 때마다 parseInt로 다시 읽었습니다.
 * 이제는 필드로 바로 들고 다니므로 오타 위험과 문자열 파싱 비용이 사라집니다.
 *
 * ZombieBossArrow와 같은 방식으로 바닐라 화살 타입(EntityType.ARROW)을 그대로 쓰기 때문에
 * 별도의 엔티티 등록·렌더러가 필요 없습니다. (클라이언트에는 평범한 화살로 보입니다)
 *
 * 수명이 다하면 스스로 사라지므로, 월드 전체 엔티티를 1초마다 순회하던 정리 루프가 필요 없습니다.
 */
public class TurretArrow extends Arrow {

    /** 사격 모드: 0 일반, 1 부채꼴, 2 점사, 3 넉백, 4 동시 강공격 */
    private int shotMode = 0;
    private boolean passBlocks = false;
    private int poisonLevel = 0;
    private int witherLevel = 0;
    private int slownessLevel = 0;
    private int weaknessLevel = 0;
    private int maxLifeTicks = 200;

    public TurretArrow(Level level, LivingEntity shooter) {
        super(level, shooter);
        this.maxLifeTicks = TurretConfig.arrowLifetime();
    }

    @Override
    public void tick() {
        super.tick();
        // 수명 초과 시 자동 소멸 (서버 전용)
        if (!this.level().isClientSide && this.tickCount > this.maxLifeTicks) {
            this.discard();
        }
    }

    // 넉백(3)·강공격(4) 화살은 맞힌 몹의 무적 시간을 리셋합니다.
    public boolean resetsInvulnerability() { return this.shotMode == 3 || this.shotMode == 4; }

    public int getShotMode() { return shotMode; }
    public void setShotMode(int shotMode) { this.shotMode = shotMode; }
    public boolean isPassBlocks() { return passBlocks; }
    public void setPassBlocks(boolean passBlocks) { this.passBlocks = passBlocks; }
    public int getPoisonLevel() { return poisonLevel; }
    public void setPoisonLevel(int poisonLevel) { this.poisonLevel = poisonLevel; }
    public int getWitherLevel() { return witherLevel; }
    public void setWitherLevel(int witherLevel) { this.witherLevel = witherLevel; }
    public int getSlownessLevel() { return slownessLevel; }
    public void setSlownessLevel(int slownessLevel) { this.slownessLevel = slownessLevel; }
    public int getWeaknessLevel() { return weaknessLevel; }
    public void setWeaknessLevel(int weaknessLevel) { this.weaknessLevel = weaknessLevel; }
}
