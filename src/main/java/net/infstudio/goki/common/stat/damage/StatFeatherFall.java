package net.infstudio.goki.common.stat.damage;

import net.infstudio.goki.common.utils.DataHelper;
import net.minecraft.world.entity.player.Player;

public class StatFeatherFall extends DamageSourceProtectionStat {
    public StatFeatherFall(int id, String key, int limit) {
        super(id, key, limit);
    }

    //	@Override
    public float getSecondaryBonus(int level) {
        return getFinalBonus(level * 0.1F);
    }

    @Override
    public double[] getDescriptionFormatArguments(Player player) {
        // TODO special
        double height = DataHelper.getFallResistance(player) + DataHelper.trimDecimals(getSecondaryBonus(getPlayerStatLevel(player)),
                1);
        return new double[]
                {DataHelper.trimDecimals(getBonus(getPlayerStatLevel(player)) * 100, 1), height};
        // return "Take " +
        // Helper.trimDecimals(getBonus(getPlayerStatLevel(player)) * 100, 1) +
        // "% less damage from falling more than " + height + " blocks.";

    }

    @Override
    public String[] getDefaultDamageSources() {
        return new String[]
                {"fall"};
    }

    @Override
    public double getBonus(int level) {
        return getFinalBonus(level * 0.026F);
    }


}
