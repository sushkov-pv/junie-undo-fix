package io.github.sushkovpv.junieundofix

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent

private val META_OR_CTRL_DOWN_MASK = if (SystemInfo.isMac) KeyEvent.META_DOWN_MASK else KeyEvent.CTRL_DOWN_MASK
private val LOG = logger<JunieKeyAction>()

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
        val toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW)
        val isJunieActive = toolWindow?.id == "ElectroJunToolWindow"
        e.presentation.isEnabled = isJunieActive
        e.presentation.isVisible = false
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        e.simulateComposeKeyEvent(modifiers, keyCode)
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

private fun AnActionEvent.simulateComposeKeyEvent(
    modifiers: Int,
    keyCode: Int,
) {
    val toolWindow = getData(PlatformDataKeys.TOOL_WINDOW) ?: return
    val skiaLayer = toolWindow.component.findSkiaLayer() ?: run {
        LOG.error("Failed to find SkiaLayer component in tool window ${toolWindow.id}")
        return
    }

    val keyPress = KeyEvent(skiaLayer, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)
    val keyRelease = KeyEvent(skiaLayer, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)

    skiaLayer.keyListeners.forEach {
        it.keyPressed(keyPress)
        it.keyReleased(keyRelease)
    }
}
