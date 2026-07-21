package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

public interface IAngelAttack {
    // 3연발 혹은 단발 공격 시퀀스를 시작하는 메서드
    void startAttackSequence(LivingEntity target);
    Monster asMonster();
}