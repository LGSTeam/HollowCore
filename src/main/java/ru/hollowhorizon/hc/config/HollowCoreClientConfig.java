package ru.hollowhorizon.hc.config;

import org.jetbrains.annotations.NotNull;
import ru.hollowhorizon.hc.api.utils.HollowConfig;

public class HollowCoreClientConfig implements HollowConfig.IHollowConfig {
    private HollowCoreClientConfig() {
    }

    @HollowConfig(comment = "Enable Main hero voice")
    public static boolean main_hero_voice = true;
    @HollowConfig
    public static float dialogues_volume = 1.0F;

    @NotNull
    @Override
    public HollowConfig.HollowConfigRuns getDist() {
        return HollowConfig.HollowConfigRuns.CLIENT;
    }
}
