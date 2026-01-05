package io.github.sushkovpv.junieundofix

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent

private val META_OR_CTRL_DOWN_MASK = if (SystemInfo.isMac) KeyEvent.META_DOWN_MASK else KeyEvent.CTRL_DOWN_MASK

fun Component.findSkiaLayer(): Component? {
    if (javaClass.name.contains("SkiaLayer", ignoreCase = true)) return this
    return (this as? Container)?.components?.firstNotNullOfOrNull { it.findSkiaLayer() }
}

abstract class JunieKeyAction(
    private val modifiers: Int,
    private val keyCode: Int,
) : AnAction(),
    DumbAware,
    ActionPromoter {
    override fun update(e: AnActionEvent) {
        e.updateHiddenJunieAction()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        e.simulateKeyEvent(modifiers, keyCode)
    }

    override fun promote(
        actions: List<AnAction>,
        context: DataContext,
    ) = listOf(this)
}

class JunieUndoAction : JunieKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_Z)

class JunieRedoAction :
    JunieKeyAction(
        if (SystemInfo.isMac) META_OR_CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK else META_OR_CTRL_DOWN_MASK,
        if (SystemInfo.isMac) KeyEvent.VK_Z else KeyEvent.VK_Y,
    )

class JunieBackspaceAction : JunieKeyAction(0, KeyEvent.VK_BACK_SPACE)

class JunieNewLineAction : JunieKeyAction(KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_ENTER)

class JunieCopyAction : JunieKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_C)

class JuniePasteAction : JunieKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_V)

private fun AnActionEvent.updateJunieActionAvailability() {
    presentation.isEnabledAndVisible = isJunieToolWindowActive()
}

fun AnActionEvent.isJunieToolWindowActive(): Boolean {
    val toolWindow = getData(PlatformDataKeys.TOOL_WINDOW)
    return toolWindow?.id == "ElectroJunToolWindow"
}

private fun AnActionEvent.updateHiddenJunieAction() {
    updateJunieActionAvailability()
    presentation.isVisible = false
}

private fun AnActionEvent.simulateKeyEvent(
    modifiers: Int,
    keyCode: Int,
) {
    val toolWindow = getData(PlatformDataKeys.TOOL_WINDOW) ?: return
    val skiaLayer = toolWindow.component.findSkiaLayer()

    val keyPress = KeyEvent(skiaLayer, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)
    val keyRelease = KeyEvent(skiaLayer, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)

    skiaLayer?.keyListeners?.forEach {
        it.keyPressed(keyPress)
        it.keyReleased(keyRelease)
    }
}
