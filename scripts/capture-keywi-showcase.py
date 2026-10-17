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
    shell("uiautomator", "dump", "/sdcard/keywi-window.xml")
    return list(ET.fromstring(shell("cat", "/sdcard/keywi-window.xml")).iter("node"))

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
ime = next(line.strip() for line in shell("ime", "list", "-s").splitlines() if PKG in line)
shell("ime", "enable", ime)
shell("ime", "set", ime)
shell("am", "force-stop", PKG)
shell("run-as", PKG, "mkdir", "-p", "shared_prefs")
prefs = '<?xml version="1.0" encoding="utf-8"?><map><string name="active_facet">HK_LIKE</string></map>'
adb("shell", f"run-as {PKG} sh -c 'cat > shared_prefs/keywi_gem_cut_preferences.xml'", input=prefs, text=True)
shell("am", "start", "-n", f"{PKG}/com.dessalines.thumbkey.MainActivity", "--es", "startRoute", "settings")
time.sleep(4)

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
    screenshot("hk-basic-live")
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
