"""Capture real settings pages on a disposable Android emulator (never a phone)."""
import json
import pathlib
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("settings-guide-captures")
PKG = "com.birdmachine.keywi.debug"
ACTIVITY = f"{PKG}/com.dessalines.thumbkey.MainActivity"
PAGES = [
    ("settings", "Gem cuts"),
    ("gemCuts", "Choose your geometry"),
    ("advancedLookAndFeel", "Saved themes"),
    ("advancedLookAndFeelEditor", "Enable custom keyboard backgrounds"),
    ("lookAndFeel", "Display mode"),
    ("advancedBoards", "Add board"),
    ("advancedKeyWordSelection", "MATRIX"),
    ("advancedInput", "Gestures & typing behavior"),
    ("contextEngine", "Adapt to text field"),
    ("advancedPowerOptions", "Unbridled"),
    ("clipboardSettings", "Clipboard"),
    ("backupAndRestore", "Backup"),
]


def adb(*args, **kwargs):
    return subprocess.run(["adb", *args], check=True, timeout=45, **kwargs)


def shell(*args):
    return adb("shell", *args, capture_output=True, text=True).stdout


def main():
    OUT.mkdir(exist_ok=True)
    adb("wait-for-device")
    shell("wm", "size", "1080x2400")
    shell("wm", "density", "420")
    adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
    shell("am", "start", "-n", ACTIVITY)
    time.sleep(3)
    ime = next(line.strip() for line in shell("ime", "list", "-a", "-s").splitlines() if PKG in line)
    shell("ime", "enable", ime)
    records = []
    try:
        for route, expected in PAGES:
            # Restart the activity so startRoute is read for each page. Selection
            # is unnecessary: these captures document settings, not the live IME.
            shell("am", "start", "-S", "-n", ACTIVITY, "--es", "startRoute", route)
            time.sleep(3)
            verified = False
            for attempt in range(3):
                try:
                    shell("uiautomator", "dump", "--compressed", "/sdcard/guide.xml")
                    xml = shell("cat", "/sdcard/guide.xml")
                    root = ET.fromstring(xml)
                    labels = " ".join(n.get("text", "") for n in root.iter("node"))
                    verified = expected.casefold() in labels.casefold()
                    (OUT / f"{route}.xml").write_text(xml)
                    if verified:
                        break
                except (subprocess.SubprocessError, ET.ParseError):
                    pass
                time.sleep(2)
            with (OUT / f"{route}.png").open("wb") as image:
                adb("exec-out", "screencap", "-p", stdout=image)
            records.append({"route": route, "expected": expected, "verified": verified})
    finally:
        (OUT / "manifest.json").write_text(json.dumps({
            "commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(),
            "device": "Pixel 6 emulator, API 34, 1080x2400, density 420",
            "pages": records,
        }, indent=2))
        (OUT / "logcat.txt").write_text(shell("logcat", "-d", "-t", "500"))
    if not all(record["verified"] for record in records):
        raise RuntimeError("Some page titles did not match; inspect manifest and images before publishing")


if __name__ == "__main__":
    main()
