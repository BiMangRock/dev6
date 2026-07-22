package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GoddessFireballEntity extends ThrowableItemProjectile {

    public GoddessFireballEntity(EntityType<? extends GoddessFireballEntity> type, Level level) {
        super(type, level);
    }

    public GoddessFireballEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.GODDESS_FIREBALL.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        // 별도의 geo/animation 모델 없이, 바닐라 화염구(Fire Charge) 아이템 렌더러를 상속하여 리소스 관리 최소화
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            // 무한 비행을 방지하기 위해 5초(100틱) 후 소멸
            if (this.tickCount >= 100) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide) {
            if (result.getEntity() instanceof LivingEntity target) {
                // 아군, 플레이어, 자신은 피해 대상에서 제외 (오사 차단)
                if (target == this.getOwner() || IAlly.isAllyEntity(target)) {
                    return;
                }

                // 좀비 진형만 공격 및 화염 피해 부여
                if (IZombieTribe.isZombieTribe(target)) {
                    target.hurt(DamageSource.thrown(this, this.getOwner()), 5.0F); // 5 데미지 가량 타격
                    target.setRemainingFireTicks(60); // 3초간 화염 유발
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            this.discard(); // 블록 충돌 시 소멸
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F; // 무중력 비행 설정
    }
}