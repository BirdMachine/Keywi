#!/usr/bin/env bash
# Run inside reactivecircus/android-emulator-runner's script callback.
# The action executes each YAML script line as a separate shell command,
# so multiline shell constructs must live in this actual script file.
set -Eeuo pipefail
mkdir -p showcase

(
  while true; do
    date -u '+%Y-%m-%dT%H:%M:%SZ'
    free -m
    ps -eo pid,ppid,rss,stat,etime,comm,args | grep -E '[q]emu-system|[e]mulator -avd' || true
    sleep 2
  done
) > showcase/emulator-host-watch.txt 2>&1 &
monitor_pid=$!

cleanup() {
  status=$?
  trap - EXIT
  kill "$monitor_pid" 2>/dev/null || true
  wait "$monitor_pid" 2>/dev/null || true
  (dmesg -T | tail -120 || true) > showcase/host-kernel-tail.txt 2>&1
  (journalctl -k -n 120 --no-pager || true) > showcase/host-journal-tail.txt 2>&1
  echo "capture exit status: $status" > showcase/capture-exit-status.txt
  exit "$status"
}
trap cleanup EXIT

python3 scripts/capture-keywi-showcase.py
