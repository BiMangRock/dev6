package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

public interface IGoddessAttack {
    void startAttackSequence(LivingEntity target);
    Monster asMonster();
}