package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.List;

public class CommanderAuraGoal extends Goal {
    private final ZombieCommanderEntity commander;
    private int timer = 0;
    private final int MAX_COOLDOWN = 200; // 💡 10초로 수정

    public CommanderAuraGoal(ZombieCommanderEntity commander) {
        this.commander = commander;
    }

    @Override
    public boolean canUse() { return true; }

    @Override
    public void tick() {
        timer++;
        if (!commander.level.isClientSide) {
            commander.setSyncCooldown(timer);
        }

        if (timer >= MAX_COOLDOWN) {
            timer = 0;
            if (!commander.level.isClientSide) {
                commander.triggerBuffAnimation();
                commander.level.broadcastEntityEvent(commander, (byte) 60);

                List<LivingEntity> targets = commander.level.getEntitiesOfClass(
                        LivingEntity.class,
                        commander.getBoundingBox().inflate(commander.getBuffRange()),
                        e -> IZombieTribe.isZombieTribe(e) && !e.getUUID().equals(commander.getUUID())
                );

                if (!targets.isEmpty()) {
                    for (LivingEntity target : targets) {
                        ZombieCommanderBuffHelper.applyHealthBuff(commander, target);
                    }
                    commander.recordBuffs(targets.size());
                }
            }
        }
    }
}