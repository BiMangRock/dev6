package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ZombieCommanderBuffHelper {
    public static void applyHealthBuff(ZombieCommanderEntity commander, LivingEntity target) {
        AttributeInstance healthAttribute = target.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        healthAttribute.removeModifier(ZombieCommanderEntity.AURA_HEALTH_BUFF_ID);

        double amount = 2.0 * commander.getBossLevel();

        AttributeModifier modifier = new AttributeModifier(
                ZombieCommanderEntity.AURA_HEALTH_BUFF_ID,
                "Commander Buff",
                amount,
                AttributeModifier.Operation.ADDITION
        );

        healthAttribute.addPermanentModifier(modifier);
        // 💡 target.heal() 코드가 없으므로 최대 체력만 증가합니다.
    }
}