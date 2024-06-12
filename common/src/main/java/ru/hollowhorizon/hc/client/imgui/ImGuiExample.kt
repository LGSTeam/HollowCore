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

package ru.hollowhorizon.hc.client.imgui

import imgui.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.addons.inventory
import ru.hollowhorizon.hc.client.utils.*
import ru.hollowhorizon.hc.client.utils.math.Interpolation

val first = ImGuiAnimator(0..100, 2f, ImGuiAnimator.Type.FREEZE, Interpolation.SINE_OUT)
val second by ImGuiAnimator(0..100, 2f, ImGuiAnimator.Type.LOOP, Interpolation.SINE_OUT)
val thrid by ImGuiAnimator(0..100, 2f, ImGuiAnimator.Type.REVERSE, Interpolation.SINE_OUT)


var stack = ItemStack.EMPTY

fun test() = Renderable {
    with(ImGuiMethods) {
        Minecraft.getInstance().frameTime
        val itemStack = inventory()
        if (!itemStack.isEmpty) stack = itemStack
        item(stack, 512f, 512f, border = true)
    }
}