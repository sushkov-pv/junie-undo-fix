# Junie Undo Fix

This plugin is an experimental solution to fix issues with standard hotkeys (Undo, Redo, Copy, Paste, etc.) in the Junie chat window.

> **Warning:** This is an experimental and unreliable fix. It may work in some configurations and IDE versions, and not in others. Use it at your own risk.

## Description
The plugin intercepts standard IDE actions (such as undo, copy, paste) and forces them as keyboard events directly into the Skia layer used by Junie's Compose interface.

Supported actions:
- Undo
- Redo
- Backspace
- Copy
- Paste
- Shift+Enter (New Line)

## How it works
When the `ElectroJunToolWindow` is active, the plugin searches for a `SkiaLayer` component inside it and simulates `KEY_PRESSED` and `KEY_RELEASED` events for the corresponding keys.