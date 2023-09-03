package ru.hollowhorizon.hc

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import ru.hollowhorizon.hc.config.HollowCoreCommonConfig

object LoggerLoader {
    @JvmStatic
    fun createLogger(name: String): Logger {
        val lvl = if (HollowCoreCommonConfig.debugMode) Level.ALL else Level.ERROR
        val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        builder.setStatusLevel(lvl)
        builder.setConfigurationName(name)

        builder.add(builder.newLogger(name, lvl))
        Configurator.initialize(builder.build())

        return LogManager.getLogger(name)
    }
}