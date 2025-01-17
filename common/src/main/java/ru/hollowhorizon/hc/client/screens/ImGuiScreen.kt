package ru.hollowhorizon.hc.client.screens

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.imgui.Renderable
import ru.hollowhorizon.hc.client.imgui.test

class ImGuiScreen(private val drawer: Renderable = test()): Screen(Component.empty()) {
    override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(gui, mouseX, mouseY, partialTick)
        ImguiHandler.drawFrame(drawer)
    }
}