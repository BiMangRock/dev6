package changmin.myMod.ally;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IAlly {
    // 이 인터페이스를 상속받은 엔티티는 기본적으로 아군으로 처리합니다.
    default boolean isAlly() {
        return true;
    }

    // 대상 엔티티가 아군인지 판별하는 유틸리티 메서드
    default boolean isAllyWith(Entity other) {
        if (other == null) return false;

        // 대상이 아군 인터페이스를 구현했거나, 플레이어(Player)인 경우 아군으로 판별
        return other instanceof IAlly || other instanceof Player;
    }
}