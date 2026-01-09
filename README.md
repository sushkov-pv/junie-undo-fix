# Junie Undo Fix

This plugin fixes [JUNIE-51](https://youtrack.jetbrains.com/issue/JUNIE-51): Undo (Cmd+Z) does not work in the Junie input field after various project actions like renaming or creating files.

> **Warning:** This is an experimental and unreliable fix. It may work in some configurations and IDE versions, and not in others. Use it at your own risk.

## How it works
The plugin intercepts standard IDE actions (such as undo, copy, paste) and forces them as keyboard events directly into the Skia layer used by Junie's Compose interface.

Supported actions:
- Undo
- Redo
- Backspace
- Copy
- Paste
- Shift+Enter (New Line)

When the `ElectroJunToolWindow` is active, the plugin searches for a `SkiaLayer` component inside it and simulates `KEY_PRESSED` and `KEY_RELEASED` events for the corresponding keys.