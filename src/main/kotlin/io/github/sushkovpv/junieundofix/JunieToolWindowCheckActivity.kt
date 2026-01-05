package io.github.sushkovpv.junieundofix

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager

class JunieToolWindowCheckActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("ElectroJunToolWindow")
        if (toolWindow == null) {
            thisLogger().warn("Junie Tool Window not found. Available tool windows: ${toolWindowManager.toolWindowIds.joinToString()}")
        } else {
            thisLogger().info("Junie Tool Window found")
        }
    }
}
