package changmin.myMod.zombieTribe; // 새로운 패키지 경로 지정

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

public interface IZombieTribe {

    // 기본적으로 이 인터페이스를 구현한 몹은 좀비 진형으로 취급합니다.
    default boolean isZombieTribeMember() {
        return true;
    }

    /**
     * 대상 생명체가 좀비 진형(기본 좀비 혹은 IZombieTribe 구현체)인지 검사하는 공용 메소드입니다.
     */
    static boolean isZombieTribe(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        // 1. 바닐라 좀비 계열이거나
        if (entity instanceof Zombie) {
            return true;
        }
        // 2. 이 인터페이스(IZombieTribe)를 구현한 커스텀 몹인 경우 좀비 진형으로 판정
        return entity instanceof IZombieTribe;
    }
}