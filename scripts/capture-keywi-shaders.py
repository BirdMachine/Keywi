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
import traceback
import xml.etree.ElementTree as ET

OUT = pathlib.Path("showcase")
OUT.mkdir(exist_ok=True)
PKG = "com.birdmachine.keywi.debug"
ACTIVITY = f"{PKG}/com.dessalines.thumbkey.MainActivity"
# ADB's one-shot logcat queries have been failing *after* the AVD process
# disappears. Stream logs to the host BEFORE selecting the IME so a crash
# leaves useful evidence, even if all subsequent adb commands fail.
_logcat_stream = None
_logcat_file = None


def start_logcat_stream():
    global _logcat_stream, _logcat_file
    _logcat_file = (OUT / "device-logcat-stream.txt").open("w")
    _logcat_stream = subprocess.Popen(
        ["adb", "logcat", "-v", "threadtime", "-b", "main", "-b", "system", "-b", "crash"],
        stdout=_logcat_file,
        stderr=subprocess.STDOUT,
    )


def stop_logcat_stream():
    if _logcat_stream is not None:
        _logcat_stream.terminate()
        try:
            _logcat_stream.wait(timeout=3)
        except subprocess.TimeoutExpired:
            _logcat_stream.kill()
    if _logcat_file is not None:
        _logcat_file.close()


def adb(*args, **kwargs):
    return subprocess.run(["adb", *args], check=True, timeout=45, **kwargs)


def shell(*args):
    return adb("shell", *args, capture_output=True, text=True).stdout


def save_screen(label):
    # Avoid exec-out screencap, which repeatedly disconnected this CI AVD.
    remote = f"/sdcard/keywi-{label}.png"
    shell("screencap", "-p", remote)
    adb("pull", remote, str(OUT / f"{label}.png"), capture_output=True, text=True)
    shell("rm", "-f", remote)


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
    # Android's software-rendered Pixel Launcher can display an ANR dialog
    # over Keywi. Dismiss it before attempting to scroll the settings page.
    for _ in range(3):
        nodes, xml = hierarchy()
        close = next((n for n in nodes if n.get("text") == "Close app"), None)
        if close is None:
            break
        bounds = list(map(int, re.findall(r"\d+", close.attrib["bounds"])))
        shell("input", "tap", str((bounds[0] + bounds[2]) // 2), str((bounds[1] + bounds[3]) // 2))
        time.sleep(2)
    # Recover if Android left us in the Boards subpage rather than Settings.
    nodes, xml = hierarchy()
    if any(n.get("text") == "Boards" for n in nodes):
        shell("input", "keyevent", "4")
        time.sleep(2)
    # Use the Theme Manager's dedicated test bench, rather than the very
    # long main Settings screen. It hosts the same installed Keywi IME.
    # Re-query after each swipe: the bench may be only a few screens below.
    for attempt in range(20):
        nodes, xml = hierarchy()
        fields = [node for node in nodes if node.get("class") == "android.widget.EditText"]
        if fields:
            field = fields[0]
            coords = list(map(int, re.findall(r"\\d+", field.attrib["bounds"])))
            shell("input", "tap", str((coords[0] + coords[2]) // 2), str((coords[1] + coords[3]) // 2))
            time.sleep(3)
            return
        if attempt in (0, 5, 11, 19):
            (OUT / f"theme-manager-scroll-{attempt}.xml").write_text(xml)
        shell("input", "touchscreen", "swipe", "240", "760", "240", "240", "360")
        time.sleep(0.4)
    raise RuntimeError("Cannot locate Theme Manager's test field; see scroll XML snapshots")


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
    shell("am", "start", "-S", "-n", ACTIVITY, "--es", "startRoute", "advancedLookAndFeel")
    # The AOSP system image avoids the Google Pixel Launcher ANRs seen in earlier runs.
    time.sleep(3)
    nodes, raw = hierarchy()
    (OUT / "settings-entry.xml").write_text(raw)
    find_test_field()
    # Starting Settings or force-stopping the active IME can reset Android to
    # Gboard. Re-select Keywi AFTER the editor has focus, and verify selection.
    for attempt in range(4):
        result = shell("ime", "set", ime)
        selected = shell("settings", "get", "secure", "default_input_method").strip()
        print(f"IME selection attempt {attempt}: {result.strip()} / {selected}", flush=True)
        if selected == ime:
            time.sleep(3)
            return
        time.sleep(2)
    raise RuntimeError(f"Keywi IME did not remain selected; expected {ime}")


def capture_state(ime, label, material, overlay):
    write_shader_prefs(material, overlay)
    shell("logcat", "-c")
    show_keyboard(ime)
    # Collect the cheap, targeted GLES log first. In prior runs the emulator
    # disconnected while querying Settings after the IME was selected, before
    # ANY useful shader evidence had been written to disk.
    print(f"IME selected for {label}; collecting GLES evidence", flush=True)
    shader_logs = shell("logcat", "-d", "-s", "KeywiShader:V", "*:S")
    (OUT / f"{label}-gles.txt").write_text(shader_logs)
    logs = shell("logcat", "-d", "-t", "3000")
    (OUT / f"{label}-logcat.txt").write_text(logs)
    selected = shell("settings", "get", "secure", "default_input_method").strip()
    assert selected == ime, f"Wrong selected IME in {label}: {selected}"
    (OUT / f"{label}-input-method.txt").write_text(shell("dumpsys", "input_method"))
    expected = int(material) + int(overlay)
    linked = shader_logs.count("Shader program linked")
    presented = shader_logs.count("Shader first frame presented")
    if expected:
        assert linked >= expected, f"Expected {expected} GLES links in {label}, found {linked}"
        assert presented >= expected, f"Expected {expected} successful GLES swaps in {label}, found {presented}"
        assert "Shader disabled after compile failure" not in shader_logs, f"GLSL compilation failed in {label}"
    (OUT / f"{label}-shader-proof.txt").write_text(
        f"state={label} material={material} overlay={overlay} selected_ime={selected} "
        f"linked_programs={linked} presented_first_frames={presented} expected_programs={expected}\\n"
        "Link logs prove compilation; successful swaps prove frame submission, NOT visible pixels, z-order or touch.\\n"
    )
    # UIAutomator can stall software-emulated graphics. Shader link evidence
    # is more important; collect the hierarchy on a best-effort basis.
    try:
        nodes, xml = hierarchy()
        (OUT / f"{label}.xml").write_text(xml)
        print(f"Verified {label}: linked={linked}, UI nodes={len(nodes)}", flush=True)
    except Exception as exc:
        (OUT / f"{label}-hierarchy-error.txt").write_text(str(exc))
        print(f"Shader state {label}: linked={linked}; UI dump unavailable: {exc}", flush=True)


def main():
    adb("wait-for-device")
    shell("wm", "size", "480x960")
    shell("wm", "density", "240")
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
    start_logcat_stream()
    # Baseline OFF first isolates AVD/IME instability from shader crashes.
    # Then collect shader link evidence before attempting fragile screenshots.
    states = [
        ("shader-off", False, False),
        ("shader-molten", True, False),
        ("shader-shimmer", False, True),
        ("shader-both", True, True),
    ]
    for label, material, overlay in states:
        capture_state(ime, label, material, overlay)
    (OUT / "shader-verification.txt").write_text(
        "API 34: selected actual Keywi IME; collected four GLES log states; UI dumps are best-effort.\\n"
        "Shader link and frame-swap logs do NOT prove visual correctness.\\n"
        "Screenshots attempted after collecting proof.\\n"
    )
    for label, material, overlay in states:
        try:
            write_shader_prefs(material, overlay)
            show_keyboard(ime)
            save_screen(label)
            print(f"Screenshot saved: {label}", flush=True)
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired, OSError) as exc:
            (OUT / f"{label}-screenshot-error.txt").write_text(str(exc))
            print(f"Screenshot unavailable for {label}: {exc}", flush=True)
            break



try:
    main()
except Exception:
    (OUT / "shader-failure-traceback.txt").write_text(traceback.format_exc())
    # Preserve original failure even when ADB is offline.
    for name, command in (
        ("shader-failure-logcat.txt", ("logcat", "-d", "-t", "2500")),
        ("shader-failure-ime.txt", ("dumpsys", "input_method")),
    ):
        try:
            (OUT / name).write_text(shell(*command))
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired, OSError) as exc:
            (OUT / name).write_text(f"ADB unavailable: {exc}\\n")
    raise
finally:
    stop_logcat_stream()
