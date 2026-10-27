# KeyWi Chromatic UI Lab — October 27 overnight handoff

## Mission and guardrails
Explore playful, weird, visually expressive **non-shader** KeyWi styling, interaction polish, and clearer interfaces for KeyWi's deep customization. Start from `Dev`, branch `feature/chromatic-ui-lab-2026-10-27`. Separate from previous GLSL shader branch and from `main`. No merges to Dev/main unless explicitly approved.

Aim for **working, testable changes** and an APK when feasible, not only mockups. Retain editability, sensible defaults, accessibility/contrast, low input latency, keyboard touch reliability, older Android compatibility, theme import/export integrity. Avoid shader/render-backend enhancements and avoid broad cleanup/optimization tonight (likely tomorrow's focus).

## Suggested design investigations — choose what evidence favors
- Clarify complexity with a cohesive information architecture: basic controls, expandable advanced panels, direct preview, discoverable actions, settings search or focused quick-jumps.
- Dramatic but lightweight styling: facet-aware borders, inset/beveled/glossy controls, visual depth with conventional gradients/draw paths, ornamental trims, chromatic key-group motifs, luminous accents without new shader backends.
- Further customize per-key/per-group feedback fades, gradient alignment and key materials where existing infrastructure allows.
- Better theme editor affordances: image/GIF visibility, readable labels, current theme vs saved theme separation, safe preview/undo, import/export clarity.
- Explore questions opportunistically while CI runs. Document prototypes, alternatives, tradeoffs; don't throw off the core goal.

## Workflow
- At start of every session: read this file fresh; inspect branch commits and latest CI; select one bounded and worthwhile task.
- Make incremental commits, run existing tests/CI, inspect artifacts; keep working in the current active session when possible. The scheduled sessions are fallback/recovery, **not** permission to abandon in-flight CI.
- At end of each session: update this journal with changes, commit SHAs, test/CI results, remaining work, next recommended step.
- Never claim emulator visual proof without a real emulator screenshot or measured run; never claim an installable APK until verified CI artifact exists.
- If a blocker asks for user intervention, investigate available free alternatives first.
- Do not assume live Work quota data. If fresh authoritative data is available: >=75% consumed prioritize finishing scope, >=90% consumed checkpoint rather than open major tasks.
- Sessions scheduled for 1/3/5/7/9 AM America/New_York, October 27, 2026. Stop by 10 AM and give a final status summary with best build/artifacts.

## Initial progress
- [x] Branch created from `Dev`.
- [x] Mission, safeguards, and continuation structure written.
- [ ] Audit current settings/theme editor architecture and select first UX target.
- [ ] Implement at least one meaningful non-shader styling/customization enhancement.
- [ ] Implement at least one coherent navigation/presentation improvement.
- [ ] Add tests or coverage appropriate to changes.
- [ ] Run CI/build and obtain a verified test APK if possible.
- [ ] Capture trustworthy emulator/UI evidence if feasible.
- [ ] Summarize improvements, tradeoffs, opportunities and remaining work.

## Run log
- 2026-10-27 setup: branched from Dev; no feature code committed yet. Next: inspect look-and-feel editor and Settings screens, then implement smallest viable improvement.
