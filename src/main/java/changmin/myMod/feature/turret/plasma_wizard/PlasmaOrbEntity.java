package changmin.myMod.feature.turret.plasma_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEntityTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PlasmaOrbEntity extends AbstractArrow implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // 💡 제네릭 와일드카드 매핑 문제를 해결하기 위해 EntityType<PlasmaOrbEntity>로 타입을 수정했습니다.
    public PlasmaOrbEntity(EntityType<PlasmaOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 마법 탄환처럼 일직선 비행
    }

    public PlasmaOrbEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.PLASMA_ORB.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.AIR); // 화살 아이템 줍기 방지
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (!this.level.isClientSide) {
            Entity owner = this.getOwner();
            if (target instanceof LivingEntity livingTarget) {
                // 아군 피해 방지
                if (owner instanceof IAlly && livingTarget instanceof IAlly) {
                    this.discard();
                    return;
                }

                // 기절 상태이상 없이 순수 피해량 5 가함
                livingTarget.hurt(DamageSource.indirectMagic(this, owner == null ? this : owner), 5.0F);
                this.playSound(net.minecraft.sounds.SoundEvents.TRIDENT_HIT, 1.0F, 1.2F);
            }
            this.discard();
        }
    }

    // --- 게코립 애니메이션 컨트롤러 주입 ---
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        // 더 이상 사용되지 않는 boolean 대신 ILoopType.EDefaultLoopTypes.LOOP를 사용합니다.
        event.getController().setAnimation(new AnimationBuilder().addAnimation("fly", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}