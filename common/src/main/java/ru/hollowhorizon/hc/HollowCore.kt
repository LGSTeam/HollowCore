/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.hollowhorizon.hc


import org.apache.logging.log4j.Logger
import ru.hollowhorizon.hc.api.HollowMod
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler
import ru.hollowhorizon.hc.common.HollowCoreCommon
import ru.hollowhorizon.hc.common.config.HollowCoreConfig
import ru.hollowhorizon.hc.common.config.hollowConfig
import ru.hollowhorizon.hc.common.registry.HollowModProcessor.initMod


object HollowCore {
    const val MODID: String = "hollowcore"

    @JvmField
    val LOGGER: Logger = ru.hollowhorizon.hc.LOGGER

    val config by hollowConfig(::HollowCoreConfig, "hollowcore")


    init {
        config.save()
        HollowCoreCommon

        initMod()

        HollowSoundHandler.MODS.add("hc")
        HollowSoundHandler.MODS.add("hollowengine")
    }
}