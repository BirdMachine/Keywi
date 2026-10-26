# KeyWi development-session usage guardrails

These instructions apply to coding agents and automation working on this repository.

## Shared Work / Codex allowance policy

Before beginning a substantial new implementation step, and at natural checkpoints during a long session, obtain the *current* 5-hour and weekly Work/Codex usage percentages **only from an authenticated, authorized source** (such as ChatGPT Settings > Usage or a supported Codex App Server `account/rateLimits/read` result). Never guess usage or treat old screenshots as live data.

Use the higher percentage consumed across the applicable 5-hour and weekly windows.

- **Below 75% consumed**: normal development; favor focused commits and reproducible builds.
- **75%–89% consumed**: finish in-progress scope; avoid starting large new features, refactors or broad CI changes. Commit cohesive increments and document remaining work.
- **90% or more consumed**: stop starting substantial tasks. Commit safe, working changes, record verification and blockers, and hand off the remaining work in a clear checkpoint. Do not push untested speculative changes simply to make a commit.
- **No fresh trustworthy reading available**: do not claim the quota is known or that a threshold has been checked. Keep the scope bounded, commit incremental work, and tell the user that automatic enforcement is unavailable until usage telemetry is connected.

The rate-limit windows can reset independently. Recalculate from current readings rather than carrying forward a prior threshold. Never automatically purchase credits or redeem resets.

## Shader feature branch

Keep GLSL/Shadertoy experimental implementation separate from `main` until validated. Target a minimally testable debug APK with:
1. a Shadertoy-style GLSL renderer;
2. one theme-background material shader;
3. one independently composited shader-effect overlay;
4. safe opt-in presets, performance limits and fallback on renderer error.

Do not present an APK as built or tested until a CI artifact or local build has actually succeeded.
