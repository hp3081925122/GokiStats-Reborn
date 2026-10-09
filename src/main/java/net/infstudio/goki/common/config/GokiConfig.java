package net.infstudio.goki.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * GokiStats mod config, in Forge config specification
 */
public class GokiConfig {
    public static final ModConfigSpec serverSpec;
    public static final Server SERVER;
    static {
        final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class Server {
        public static boolean initiativeSync = false;

        public final ModConfigSpec.IntValue syncTicks;
        public final ModConfigSpec.DoubleValue globalCostMultiplier;
        public final ModConfigSpec.DoubleValue globalLimitMultiplier;
        public final ModConfigSpec.DoubleValue globalBonusMultiplier;
        public final ModConfigSpec.BooleanValue loseStatsOnDeath;
        public final ModConfigSpec.DoubleValue loseStatsMultiplier;
        public final ModConfigSpec.IntValue globalMaxRevertLevel;
        public final ModConfigSpec.DoubleValue globalRevertFactor;
        public final ModConfigSpec.IntValue reaperLimit;

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("服务端配置 / Server configuration settings")
                    .push("server");

            syncTicks = builder.comment("主动同步间隔，默认 400 刻（20 秒），0 禁用 / Ticks for initiative sync, default for 400 ticks (20s). Set to 0 to disable this feature")
                    .defineInRange("syncTicks", 400, 0, Integer.MAX_VALUE);
            globalCostMultiplier = builder.comment("所有技能升级经验的全局倍率 / A global multiplier on the cost to upgrade all stats")
                    .defineInRange("globalCostMultiplier", 1, 0, Double.MAX_VALUE);
            globalLimitMultiplier = builder.comment("所有技能等级上限的全局倍率 / A global multiplier on the max level limit of all stats")
                    .defineInRange("globalLimitMultiplier", 2.5, 0, Double.MAX_VALUE);
            globalBonusMultiplier = builder.comment("所有技能效果的全局倍率 / A global multiplier on the bonus all stats gives")
                    .defineInRange("globalBonusMultiplier", 1, 0, Double.MAX_VALUE);
            loseStatsOnDeath = builder.comment("死亡时是否损失技能等级 / Does your stats lose on death")
                    .define("loseStatsOnDeath", false);
            loseStatsMultiplier = builder.comment("死亡损失等级的比例，范围 0 到 1 / Multiplier of levels you will lose, between 0-1")
                    .defineInRange("loseStatsMultiplier", 1, 0d, 1d);
            globalMaxRevertLevel = builder.comment("每项技能允许退还的次数，-1 不限，0 禁用 / An integer that constrains the max number of amount of the skill can be reverted. -1 for no limit. 0 to disable reverting.")
                    .defineInRange("globalMaxRevertLevel", -1, -1, Integer.MAX_VALUE);
            globalRevertFactor = builder.comment("退还技能等级时返还经验的比例，范围 0 到 1 / How much percentage of exp will be given back to player if a player revert a skill, between 0-1.")
                    .defineInRange("globalRevertFactor", 0.8, 0d, 1d);

            reaperLimit = builder.comment("收割技能可作用目标的最大生命值，-1 不限 / When using reaper skill, the maximum health of target hostile. -1 for no limit.")
                    .defineInRange("reaperLimit", 20, -1, Integer.MAX_VALUE);
        }
    }
}
