package ru.hollowhorizon.hc.config

import ru.hollowhorizon.hc.api.utils.HollowConfig

object HollowCoreCommonConfig: HollowConfig.IHollowConfig {
    @JvmField @HollowConfig
    val debugMode = false
}