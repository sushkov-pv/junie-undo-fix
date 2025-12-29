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

abstract class InvisibleKeyAction(
    private val modifiers: Int,
    private val keyCode: Int,
) : AnAction(),
    DumbAware,
    ActionPromoter {
    override fun update(e: AnActionEvent) {
        e.updateInvisibleAction()
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

class ChatUndoAction : InvisibleKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_Z)

class ChatRedoAction :
    InvisibleKeyAction(
        if (SystemInfo.isMac) META_OR_CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK else META_OR_CTRL_DOWN_MASK,
        if (SystemInfo.isMac) KeyEvent.VK_Z else KeyEvent.VK_Y,
    )

class ChatBackspaceAction : InvisibleKeyAction(0, KeyEvent.VK_BACK_SPACE)

class ChatNewLineAction : InvisibleKeyAction(KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_ENTER)

class ChatCopyAction : InvisibleKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_C)

class ChatPasteAction : InvisibleKeyAction(META_OR_CTRL_DOWN_MASK, KeyEvent.VK_V)

private fun AnActionEvent.updateVisibleAction() {
    presentation.isEnabledAndVisible = isJunieToolWindowActive()
}

fun AnActionEvent.isJunieToolWindowActive(): Boolean {
    val toolWindow = getData(PlatformDataKeys.TOOL_WINDOW)
    return toolWindow?.id == "ElectroJunToolWindow"
}

private fun AnActionEvent.updateInvisibleAction() {
    updateVisibleAction()
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
