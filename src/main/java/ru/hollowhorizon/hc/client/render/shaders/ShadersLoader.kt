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

package ru.hollowhorizon.hc.client.render.shaders

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event

object ShadersLoader : ResourceManagerReloadListener {
    val shaders = arrayListOf<ShaderProgram>()

    init {
        MinecraftForge.EVENT_BUS.post(RegisterHollowShadersEvent(shaders))
    }

    override fun onResourceManagerReload(manager: ResourceManager) {
        shaders.forEach { it.destroy() }
        shaders.clear()

        MinecraftForge.EVENT_BUS.post(RegisterHollowShadersEvent(shaders))
    }
}

class RegisterHollowShadersEvent(private val shaders: MutableList<ShaderProgram>) : Event() {
    fun create(shaders: List<ResourceLocation>, uniforms: List<Uniform>): ShaderProgram {
        val shader = ShaderProgram(shaders, uniforms)
        this.shaders.add(shader)
        return shader
    }
}