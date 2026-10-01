AI WAS USED IN SOME PARTS OF THE CODE SINCE I COULDNT FIGURE SOME THINGS OUT, LIKE KEYSTROKES

# 🐰 Bunny Client (Minecraft 1.21.11)

A lightweight, high-performance standalone client mod for Minecraft **1.21.11**, designed with **VulkanMod** compatibility at its core. Like Feather Client once offered, Bunny Client is a standalone Fabric mod — meaning **no separate launcher is required**! Simply drop the `.jar` into your normal `.minecraft/mods` folder and launch the game through your favorite launcher (Vanilla launcher, Prism, Modrinth, ATLauncher, or CurseForge).

---

## ✨ Features

- **⚡ VulkanMod & Sodium Compatibility**:
  - Engineered with **zero raw OpenGL / GL11 calls**.
  - All HUD rendering, screen drawing, and batching strictly leverage Minecraft's native `DrawContext` pipeline.
  - Runs natively on **VulkanMod**, **Sodium**, **Iris**, **Nvidium**, and standard vanilla OpenGL with zero crashes or flickering.
- **📊 HUD Modules (Fully Draggable & Customizable)**:
  - **FPS Counter**: Real-time FPS with custom prefix, styling, and background toggles.
  - **Coordinates & Biome**: Precise X, Y, Z coordinates, player facing direction, biome name, and automatic Nether-to-Overworld coordinate calculations.
  - **Armor & Item Status**: Equipped helmet, chestplate, leggings, boots, and held items with live durability values, damage counters, and item stacks.
  - **Potion Status**: Active status effects with roman numeral amplifiers and countdown timers (`MM:SS`).
  - **Keystrokes**: Reactive WASD, Spacebar, LMB, and RMB keys with dynamic keypress animations and embedded CPS.
  - **CPS Counter**: Left and Right clicks-per-second tracker with rolling 1-second interval.
  - **Ping / Latency**: Real-time server latency in milliseconds.
  - **Sprint / Sneak Status**: Visual status badge when auto-sprint or sneak is active.
  - **Interactive HUD Editor**: Press **"Edit HUD Layout"** in the client menu to drag-and-drop any HUD module anywhere on your screen with edge-snapping and guideline alignment.
- **🛠️ Quality of Life (QOL)**:
  - **Smooth Zoom**: OptiFine/Lunar-style smooth cinematic zoom with customizable target FOV.
  - **Fullbright**: Built-in gamma boost to see in dark caves and underwater.
  - **Toggle Sprint & Toggle Sneak**: Auto-sprinting and sneak toggling without holding down keys.
  - **Hitbox Toggle**: Toggle entity bounding boxes anytime without fumbling with `F3 + B`.
  - **Custom Crosshair**: Toggle custom dot, cross, or minimalist crosshairs.
- **🚀 Performance Optimizations**:
  - **Entity Frustum Culling**: Skips rendering calculations for occluded entities located behind the player.
  - **Particle Limiter**: Smart limiter during explosion and combat particle spam to maintain smooth FPS.
- **🎨 Sleek Lunar-Inspired Aesthetics & Theme**:
  - **Palette**: Hand-crafted color scheme featuring `#D6C8C3` (warm linen cream) and `#B9C0C4` (cool slate silver) over deep slate card backgrounds (`#16171B`).
  - **Satisfying Sound Effects**: Crisp, tactile audio clicks with pitch variations when clicking buttons, switching tabs, or toggling switches.
  - **Custom Lunar-Style Title Screen**: A minimalist main menu with quick actions, version info, and an instant toggle to switch back to vanilla at any time.
  - **Bunny Badge (`🐰`)**: A cute bunny badge rendered directly before your name in nametags and menus for Bunny Client players.
- **💬 Discord Rich Presence (RPC)**:
  - Lightweight pure-Java Discord IPC client connecting to local IPC pipes with **no native DLL dependencies** (prevents crashes across Windows, Linux, and ARM).
  - Displays game status, singleplayer/multiplayer server address, elapsed play time, and custom Bunny Client assets.

---



## 📦 Installation & Setup

1. Make sure you have **Fabric Loader** installed for Minecraft **1.21.11** (or 1.21.1).
2. Download and install the **Fabric API** mod for your version.
3. *(Optional but Recommended)*: Install **VulkanMod** or **Sodium** for extreme FPS performance.
4. Place `bunny-client-1.0.0.jar` into your Minecraft `.minecraft/mods` directory.
5. Launch Minecraft using your usual launcher.
6. Press `Right Shift` in-game to open the Bunny Client menu!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
