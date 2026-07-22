package changmin.changmin_villager_turret.feature.zombie.demon1;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Demon1BatEntity extends AbstractArrow {
    private LivingEntity target; // 추적할 공격 대상

    // 레지스트리 등록용 생성자
    public Demon1BatEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    // 소환용 생성자 (타겟 정보 추가)
    public Demon1BatEntity(Level level, LivingEntity shooter, LivingEntity target) {
        super(ModEntityTypes.DEMON1_BAT.get(), level);
        this.setOwner(shooter);
        this.target = target;
        this.setNoGravity(true); // 중력 없는 투사체 설정
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 회수 불가능
    }

    @Override
    public void tick() {
        super.tick();

        // 실시간 유도(Homing) 로직: 타겟이 유효하고 살아있다면 대상을 조준해 비행 궤적을 곡선으로 꺾음
        if (this.target != null && this.target.isAlive()) {
            Vec3 currentMovement = this.getDeltaMovement();
            Vec3 targetPos = this.target.getEyePosition(); // 대상의 눈높이를 향해 유도
            Vec3 toTarget = targetPos.subtract(this.position()).normalize();

            // 기존 속도 방향과 유도 방향을 부드럽게 병합 (유도 성능 계수: 0.12D)
            double homingStrength = 0.12D;
            Vec3 blended = currentMovement.scale(1.0 - homingStrength).add(toTarget.scale(homingStrength));

            // 박쥐 투사체 속도 유지 (1.2D)
            // 예시: 유도 비행 시의 유지 속도를 0.7D 로 감소
            double speed = 0.7D;
            this.setDeltaMovement(blended.normalize().scale(speed));
        }

        if (!this.level.isClientSide) {
            // 성능 향상과 메모리 최적화를 위해 10초(200틱)가 지나면 자연 소멸
            if (this.tickCount >= 200) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 본인(소환사) 타격 방지
        if (result.getEntity() == this.getOwner()) {
            return;
        }
        // 피격 대상이 LivingEntity 일 때만 좀비 부족 체크 진행
        if (result.getEntity() instanceof LivingEntity livingEntity) {
            if (IZombieTribe.isZombieTribe(livingEntity)) {
                return;
            }
        }
        super.onHitEntity(result);
    }
}