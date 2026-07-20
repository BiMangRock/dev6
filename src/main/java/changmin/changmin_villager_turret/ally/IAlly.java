package changmin.changmin_villager_turret.ally;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.AbstractVillager; // 💡 AbstractVillager 대신 오직 '일반 주민'만 정확히 지칭

public interface IAlly {
    // 이 인터페이스를 상속받은 엔티티는 기본적으로 아군으로 처리합니다.
    default boolean isAlly() {
        return true;
    }

    // 대상 엔티티가 아군인지 판별하는 유틸리티 메서드
    default boolean isAllyWith(Entity other) {
        if (other == null) return false;

        // 💡 [컴파일 에러 해결]: 인터페이스의 'this'는 Entity가 아니므로, instanceof를 활용해 Entity 객체로 안전하게 변환하여 전달합니다.
        if (this instanceof Entity thisEntity) {
            return isAllyEntity(thisEntity) && isAllyEntity(other);
        }
        return false;
    }

    // 대상이 아군 진형인지 통합 판별하는 스태틱 메서드
    static boolean isAllyEntity(Entity entity) {
        if (entity == null) return false;

        // 💡 [우민 방지]: 일반 주민(Villager) 클래스만 검사하므로 우민(Pillager 등)은 철저히 적으로 배제됩니다.
        return entity instanceof IAlly || entity instanceof Player || entity instanceof AbstractVillager;
    }
}