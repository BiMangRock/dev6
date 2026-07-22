package changmin.changmin_villager_turret.feature.zombie.demon1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

public interface IDemon1Attack {
    void startAttackSequence(LivingEntity target);
    Monster asMonster();
}