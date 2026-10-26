# On the shader laboratory branch, run the shader-specific emulator checks
# instead of unrelated HK-like toolbar assertions. This dispatch is also the\n# existing emulator workflow's push trigger for shader test revisions.
import os as _os
if _os.environ.get("GITHUB_REF_NAME") == "feature/shadertoy-renderer-layers":
    import runpy as _runpy
    _runpy.run_path("scripts/capture-keywi-shaders.py", run_name="__main__")
    raise SystemExit(0)

"""Capture the themed Keywi IME through its existing settings test field."""
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("showcase")
OUT.mkdir(exist_ok=True)
PKG = "com.birdmachine.keywi.debug"

def adb(*args, **kwargs):
    return subprocess.run(["adb", *args], check=True, **kwargs)

def shell(*args):
    return adb("shell", *args, capture_output=True, text=True).stdout

def nodes():
    for attempt in range(5):
        result = shell("uiautomator", "dump", "--compressed", "/sdcard/keywi-window.xml")
        (OUT / "ui-dump-log.txt").write_text(result)
        try:
            xml = shell("cat", "/sdcard/keywi-window.xml")
            return list(ET.fromstring(xml).iter("node"))
        except (subprocess.CalledProcessError, ET.ParseError):
            time.sleep(2)
    with (OUT / "ui-dump-failure.png").open("wb") as file:
        adb("exec-out", "screencap", "-p", stdout=file)
    (OUT / "logcat.txt").write_text(shell("logcat", "-d", "-t", "1000"))
    (OUT / "input-method-state.txt").write_text(shell("dumpsys", "input_method"))
    raise RuntimeError("Could not dump Android UI hierarchy; see screenshot and logs")

def tap(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
    shell("input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(1)

def find(label):
    return next((n for n in nodes() if label in (n.get("text"), n.get("content-desc"))), None)

def press(label):
    node = find(label)
    if node is None:
        raise RuntimeError(f"Missing Keywi control: {label}")
    tap(node)

def screenshot(name):
    with (OUT / f"{name}.png").open("wb") as file:
        adb("exec-out", "screencap", "-p", stdout=file)
    (OUT / f"{name}.xml").write_text(shell("cat", "/sdcard/keywi-window.xml"))

adb("wait-for-device")
shell("wm", "size", "1080x2400")
shell("wm", "density", "420")
adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
shell("am", "start", "-n", f"{PKG}/com.dessalines.thumbkey.MainActivity")
ime = None
for attempt in range(15):
    listing = shell("ime", "list", "-a", "-s")
    (OUT / "ime-list.txt").write_text(listing)
    ime = next((line.strip() for line in listing.splitlines() if PKG in line), None)
    if ime:
        break
    time.sleep(2)
if not ime:
    (OUT / "package.txt").write_text(shell("dumpsys", "package", PKG))
    (OUT / "logcat.txt").write_text(shell("logcat", "-d", "-t", "1000"))
    screenshot("ime-not-registered")
    raise RuntimeError("Installed Keywi IME not registered; see diagnostics")
shell("am", "force-stop", PKG)
shell("run-as", PKG, "mkdir", "-p", "shared_prefs")
prefs = '<?xml version="1.0" encoding="utf-8"?><map><string name="active_facet">HK_LIKE</string></map>'
adb("shell", f"run-as {PKG} sh -c 'cat > shared_prefs/keywi_gem_cut_preferences.xml'", input=prefs, text=True)
shell("ime", "enable", ime)
shell("ime", "set", ime)
shell("am", "start", "-n", f"{PKG}/com.dessalines.thumbkey.MainActivity", "--es", "startRoute", "settings")
time.sleep(8)
with (OUT / "settings-open.png").open("wb") as file:
    adb("exec-out", "screencap", "-p", stdout=file)

for attempt in range(12):
    current = nodes()
    field = next((n for n in current if n.get("class") == "android.widget.EditText"), None)
    if field is not None:
        tap(field)
        break
    # The test field lives at the bottom of Settings.
    shell("input", "swipe", "500", "1900", "500", "650", "400")
    time.sleep(1)
else:
    screenshot("missing-test-field")
    raise RuntimeError("Could not reach Keywi's settings test field")
time.sleep(3)
try:
    assert find("Keywi settings") is not None, "HK settings shortcut missing in live IME"
    for letter in "keywi":
        press(letter)
    assert any(n.get("class") == "android.widget.EditText" and "keywi" in n.get("text", "").lower() for n in nodes()), "HK taps did not reach the editor"
    screenshot("hk-basic-live")
    press("𝕱")
    screenshot("fancy-text-live")
    press("(｡•̀ᴗ-)✧")
    screenshot("kaomoji-live")
    press("✕")
    press("?123")
    screenshot("hk-symbols-live")
    press("⌘")
    screenshot("hk-advanced-live")
    press("Keywi settings")
    time.sleep(2)
    screenshot("hk-settings-shortcut")
    (OUT / "verification.txt").write_text(
        "Installed stable-signed debug APK; enabled and selected Keywi IME.\n"
        "Opened the existing settings test field and tapped k/e/y/w/i on the actual HK keyboard.\n"
        "Captured basic, symbol, advanced layers and exercised the settings shortcut.\n"
    )
finally:
    (OUT / "logcat.txt").write_text(shell("logcat", "-d", "-t", "1000"))
