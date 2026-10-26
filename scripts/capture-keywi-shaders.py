"""Emulator smoke test of actual Keywi shader material and overlay.

Runs against a real Android emulator in GitHub Actions, not a mock Compose preview.
Captures four opt-in configurations with UI dumps, logs and screenshots.
Screenshots alone do not prove SurfaceView layering; inspect them before claiming
visual correctness.
"""
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("showcase")
OUT.mkdir(exist_ok=True)
PKG = "com.birdmachine.keywi.debug"
ACTIVITY = f"{PKG}/com.dessalines.thumbkey.MainActivity"


def adb(*args, **kwargs):
    return subprocess.run(["adb", *args], check=True, **kwargs)


def shell(*args):
    return adb("shell", *args, capture_output=True, text=True).stdout


def save_screen(label):
    with (OUT / f"{label}.png").open("wb") as stream:
        adb("exec-out", "screencap", "-p", stdout=stream)


def hierarchy():
    last_error = None
    for attempt in range(4):
        try:
            shell("uiautomator", "dump", "--compressed", "/sdcard/keywi-shader-ui.xml")
            raw = shell("cat", "/sdcard/keywi-shader-ui.xml")
            return list(ET.fromstring(raw).iter("node")), raw
        except (subprocess.CalledProcessError, ET.ParseError) as exc:
            last_error = exc
            time.sleep(2)
    raise RuntimeError(f"Android UI hierarchy unavailable: {last_error}")


def find_test_field():
    for attempt in range(15):
        nodes, xml = hierarchy()
        fields = [node for node in nodes if node.get("class") == "android.widget.EditText"]
        if fields:
            field = fields[0]
            coords = list(map(int, re.findall(r"\d+", field.attrib["bounds"])))
            shell("input", "tap", str((coords[0] + coords[2]) // 2), str((coords[1] + coords[3]) // 2))
            time.sleep(3)
            return
        shell("input", "swipe", "500", "1900", "500", "650", "400")
        time.sleep(1)
    raise RuntimeError("Cannot locate the Settings test field; see diagnostics")


def write_shader_prefs(material, overlay):
    # Set before restarting the process; the keyboard reads these when composed.
    shell("am", "force-stop", PKG)
    prefs = (
        '<?xml version="1.0" encoding="utf-8" standalone="yes" ?><map>'
        f'<boolean name="material" value="{str(material).lower()}" />'
        f'<boolean name="overlay" value="{str(overlay).lower()}" />'
        '</map>'
    )
    adb(
        "shell",
        f"run-as {PKG} sh -c 'mkdir -p shared_prefs && cat > shared_prefs/keywi_shader_experiment.xml'",
        input=prefs,
        text=True,
    )
    recorded = shell("run-as", PKG, "cat", "shared_prefs/keywi_shader_experiment.xml")
    assert f'value="{str(material).lower()}"' in recorded
    assert f'value="{str(overlay).lower()}"' in recorded


def show_keyboard(ime):
    shell("ime", "enable", ime)
    shell("ime", "set", ime)
    shell("am", "start", "-n", ACTIVITY, "--es", "startRoute", "settings")
    time.sleep(5)
    find_test_field()


def capture_state(ime, label, material, overlay):
    write_shader_prefs(material, overlay)
    shell("logcat", "-c")
    show_keyboard(ime)
    time.sleep(3)
    save_screen(label)
    nodes, xml = hierarchy()
    (OUT / f"{label}.xml").write_text(xml)
    (OUT / f"{label}-input-method.txt").write_text(shell("dumpsys", "input_method"))
    logs = shell("logcat", "-d", "-t", "2500")
    (OUT / f"{label}-logcat.txt").write_text(logs)
    if material or overlay:
        assert "Shader program linked" in logs, f"No GLES shader linked in {label}; inspect logcat"
        assert "Shader disabled after compile failure" not in logs, f"GLSL compilation failed in {label}"
    print(f"Captured {label}: material={material}, overlay={overlay}, UI nodes={len(nodes)}")


def main():
    adb("wait-for-device")
    shell("wm", "size", "1080x2400")
    shell("wm", "density", "420")
    adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
    shell("am", "start", "-n", ACTIVITY)
    ime = None
    for attempt in range(15):
        candidates = shell("ime", "list", "-a", "-s").splitlines()
        ime = next((line.strip() for line in candidates if PKG in line), None)
        if ime:
            break
        time.sleep(2)
    if not ime:
        raise RuntimeError("Installed Keywi IME not registered")
    for label, material, overlay in [
        ("shader-off", False, False),
        ("shader-molten", True, False),
        ("shader-shimmer", False, True),
        ("shader-both", True, True),
    ]:
        capture_state(ime, label, material, overlay)
    (OUT / "shader-verification.txt").write_text(
        "Android emulator API 34: installed and enabled actual Keywi IME.\n"
        "Captured off / molten / shimmer / both screenshots, UI hierarchies and logs.\n"
        "Verified GLES program link log for enabled states and no logged compile failures.\n"
        "Visual z-order, transparency, readability and touch must still be reviewed in captures.\n"
    )


try:
    main()
except Exception:
    save_screen("shader-failure")
    (OUT / "shader-failure-logcat.txt").write_text(shell("logcat", "-d", "-t", "2500"))
    (OUT / "shader-failure-ime.txt").write_text(shell("dumpsys", "input_method"))
    raise
