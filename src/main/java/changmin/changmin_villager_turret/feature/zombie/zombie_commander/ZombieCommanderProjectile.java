package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ZombieCommanderProjectile extends ThrowableItemProjectile {
    private float maggotAtk = 2.0F;
    private int maggotLife = 200;
    private double maggotHp = 6.0D;

    public ZombieCommanderProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public ZombieCommanderProjectile(Level level, LivingEntity owner) {
        super(ModEntityTypes.COMMANDER_PROJECTILE.get(), owner, level);
    }

    // 💡 해결: Goal에서 호출하던 스탯 설정 메서드 구현
    public void setMaggotStats(float atk, int life, double hp) {
        this.maggotAtk = atk;
        this.maggotLife = life;
        this.maggotHp = hp;
    }

    @Override
    protected Item getDefaultItem() { return Items.SLIME_BALL; }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            ZombieCommanderMaggotEntity maggot = new ZombieCommanderMaggotEntity(ModEntityTypes.COMMANDER_MAGGOT.get(), this.level);
            maggot.setPos(this.getX(), this.getY(), this.getZ());

            // 💡 소환되는 구더기에 커맨더의 업그레이드 수치 주입
            if (maggot.getAttribute(Attributes.ATTACK_DAMAGE) != null)
                maggot.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.maggotAtk);
            if (maggot.getAttribute(Attributes.MAX_HEALTH) != null)
                maggot.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.maggotHp);

            maggot.setHealth((float)this.maggotHp);
            maggot.setLifeTicks(this.maggotLife); // 구더기 수명 설정

            if (this.getOwner() != null) maggot.setOwner(this.getOwner().getUUID());

            this.level.addFreshEntity(maggot);
            this.discard();
        }
    }
}