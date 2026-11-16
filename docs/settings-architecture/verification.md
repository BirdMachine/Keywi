# Verification and review notes

## Scope verified

This branch adds documentation only under `docs/settings-architecture/`. It does not change Kotlin, resources, Android navigation, preference schemas, tests, build configuration, the existing October audit, or the independently edited key-rendering/editor files. PR target is `Dev`.

- Reviewed baseline `8ef014017a020346da3d3e2f8922bb184a51688b`; 23 registered activity destinations.
- 275 unique catalog IDs with current breadcrumb, control behavior/choices, source file/line, store scope and a destination in all three proposals.
- 39 mapped leaf paths per proposal (including shared editor surfaces and Home quick controls). Seven Creative Studio families, eight Instrument & Controls families, six Hybrid families. Not every leaf is required to become a separate route.
- Six mobile artboards: three alternative homes, light Hybrid home, advanced Tap effects, Keys & groups. Two comparison sheets. Standalone SVG plus rendered PNG versions.
- Read issue #63, issue #62 and the open PR summaries for #56/#57/#58/#60/#61; rechecked open PRs before publication and read #64’s current scope. PR #64 fits labels and collapses Gesture Actions / Character Values; this branch only references that boundary.
- Existing historical guide screenshots informed visual context only. They do not prove current build appearance.

## Checks performed for this documentation

Run `python docs/settings-architecture/verify_exploration.py` from the repository root:

- Verify catalog count/unique IDs and nonempty mapping fields.
- Verify every source file and line exists at the pinned commit, and each catalog ID appears in the inventory and proposal trees.
- Verify all 23 baseline routes are named in the current map and alias plan.
- Resolve local Markdown links and image references; parse all SVGs; inspect PNG dimensions.
- Check text and control-outline contrast token pairs numerically; full composed-media contrast remains a future device check.

Also run `git diff --check` after staging, and inspect the diff’s path list to confirm the documentation-only boundary. The SVGs were rendered with Sharp and the comparison sheets and light Home were visually inspected. The home comparison was regenerated after increasing Studio tile text sizes.

No Android build, unit suite, emulator, APK install, file-provider round trip, TalkBack session or user study was run: there is no app implementation in this change. Wireframe art is not a performance or accessibility certification. Runtime regression requirements are in [migration.md](migration.md).

## Review decisions

- Does Hybrid’s six-door map feel inviting and predictable? In particular, are Look & feel and Feel & sound distinct enough?
- Should the green Matrix editor remain an optional visual identity, or eventually share the surrounding settings chrome?
- Should Tap effects primarily live with appearance (recommended, consistent with #60), with a clear shortcut from feedback?
- Which six controls would be useful pins by choice? Keep recents opt-in; do not seed them with fabricated user history.
- Can the new topology-aware targeting retain manual expert IDs and all existing ME/HK assignments without conversion?
- Are any source-era controls missing from the catalog after other PRs merge? Reconcile the inventory at implementation start.

These questions are for PR review; they do not block the documentation deliverable or authorize implementing the proposed UI.
