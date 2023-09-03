package ru.hollowhorizon.hc.api.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.loading.FMLPaths
import org.apache.commons.io.FileUtils
import org.jetbrains.annotations.ApiStatus
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isLogicalClient
import ru.hollowhorizon.hc.client.utils.isLogicalServer
import ru.hollowhorizon.hc.client.utils.isPhysicalClient
import ru.hollowhorizon.hc.client.utils.isPhysicalServer
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.HashMap

annotation class HollowConfig(
    val value: String = "",
    val configPath: String = "",
    val comment: String = "",
    @ApiStatus.Experimental
    val min: Float = 0F,
    @ApiStatus.Experimental
    val max: Float = 0F
) {
    interface IHollowConfig {
        fun getFileName(): String {
            return "hollowcore/${ModLoadingContext.get().activeContainer.modId}"
        }

        fun getDist(): HollowConfigRuns {
            return HollowConfigRuns.COMMON
        }

        fun enableInGameConfig(): Boolean {
            return false
        }
    }

    object Serializer {
        private val values: MutableList<IHollowConfig> = mutableListOf()
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        @Synchronized
        @JvmStatic
        fun start() {
            this.addValues()

            val configDir = FMLPaths.CONFIGDIR.get().toFile()
            if (!configDir.exists()) configDir.mkdirs()

            val configFiles: Array<File>? = configDir.listFiles()

            var fileName = ""

            this.values.forEach { value ->
                if (value.getDist() == HollowConfigRuns.CLIENT) {
                    fileName = "client"
                } else if (value.getDist() == HollowConfigRuns.COMMON) {
                    fileName = "common"
                } else if (value.getDist() == HollowConfigRuns.SERVER) {
                    fileName = "server"
                }
            }

            if (configFiles != null) {
                val configs = hashMapOf<String, JsonObject>()
                configFiles.forEach {
                    val name = it.name.substring(0, it.name.length - (".json".length))
                    try {
                        val fileContents = FileUtils.readFileToString(it, Charsets.UTF_8)
                        val jsonObjects = gson.fromJson(fileContents, JsonObject::class.java)
                        configs[name] = jsonObjects
                    } catch (e: IOException) {
                        HollowCore.LOGGER.error("Failed to load config file by name ${it.name}", e)
                    }
                }
                deserializeJson(configs)
            }

            this.serializeJson().entries.forEach {
                val configFile = File(configDir, "${it.key}_${fileName}.json")
                val json = gson.toJson(it.value)
                try {
                    FileUtils.writeStringToFile(configFile, json, Charsets.UTF_8)
                } catch (e: IOException) {
                    throw RuntimeException("Failed to save config file: ${configFile.absolutePath}")
                }
            }
        }

        @Synchronized
        private fun getConfigFields(): HashMap<Field, HollowConfig> {
            val fieldMap = hashMapOf<Field, HollowConfig>()
            this.values.forEach { clazz ->
                for (it in clazz.javaClass.declaredFields) {
                    if (!it.isAnnotationPresent(HollowConfig::class.java)) continue
                    val annotation = it.getAnnotation(HollowConfig::class.java)
                    fieldMap[it] = annotation
                }
            }

            return fieldMap
        }

        @Synchronized
        private fun serializeJson(): HashMap<String, JsonObject> {
            val fieldMap = this.getConfigFields()
            val configs = hashMapOf<String, JsonObject>()

            this.values.forEach { value ->
                fieldMap.forEach fMap@ { maps ->
                    if ((isPhysicalClient || isLogicalClient) && (value.getDist() == HollowConfigRuns.SERVER)) return@fMap
                    else if ((isLogicalServer || isPhysicalServer) && (value.getDist() == HollowConfigRuns.CLIENT)) return@fMap
                    val field = maps.key
                    val annotation = maps.value

                    val cfg = configs.computeIfAbsent(value.getFileName()) { _ -> JsonObject() }

                    val categoryObject: JsonObject

                    if (cfg.has(annotation.configPath)) {
                        categoryObject = cfg.getAsJsonObject(annotation.configPath)
                    } else {
                        categoryObject = JsonObject()
                        cfg.add(annotation.configPath, categoryObject)
                    }

                    val key: String = annotation.value.ifEmpty { field.name }

                    if (categoryObject.has(key)) throw UnsupportedOperationException("Some bad news.. Duplicate key found: $key")

                    val fieldObject = JsonObject()

                    fieldObject.addProperty("_comment", annotation.comment)

                    val localValue: Any
                    try {
                        localValue = field.get(null)
                    } catch (e: IllegalAccessException) {
                        throw RuntimeException(e)
                    }

                    val element = gson.toJsonTree(localValue)
                    fieldObject.add("value", element)
                    categoryObject.add(key, fieldObject)
                }
            }

            return configs
        }

        @Synchronized
        private fun deserializeJson(configs: HashMap<String, JsonObject>) {
            val fieldMap = this.getConfigFields()

            this.values.forEach { value ->
                for (entry in fieldMap.entries) {
                    if ((isPhysicalClient || isLogicalClient) && (value.getDist() == HollowConfigRuns.SERVER)) break
                    else if ((isLogicalServer || isPhysicalServer) && (value.getDist() == HollowConfigRuns.CLIENT)) break

                    val field = entry.key
                    val annotation = entry.value

                    val config = configs[value.getFileName()] ?: continue

                    val categoryObj = config.getAsJsonObject(annotation.configPath) ?: continue

                    val key = field.name
                    if (!categoryObj.has(key)) continue

                    val fieldObj = categoryObj.get(key).asJsonObject
                    if (!fieldObj.has("value")) continue

                    val jsonValue = fieldObj.get("value")
                    val fieldType = field.type

                    val fieldValue = gson.fromJson(jsonValue, fieldType)

                    try {
                        field.set(null, fieldValue)
                    } catch (e: IllegalAccessException) {
                        throw RuntimeException("Failed to set field value", e)
                    }
                }
            }
        }

        @Synchronized
        private fun addValues() {
            val classLoader = ServiceLoader.load(IHollowConfig::class.java)

            for (value in classLoader) {
                this.values.add(value)
            }
        }
    }

    enum class HollowConfigRuns {
        SERVER,
        CLIENT,
        COMMON
    }
}
