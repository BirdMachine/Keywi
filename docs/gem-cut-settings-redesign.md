# Gem Cuts settings redesign

The October 12 visual direction uses compact geometry thumbnails alongside descriptions,
outlined violet selection states, and a larger selection-aware preview. Chrome adapts to
Keywi's dark/light mode without applying or saving a keyboard theme.

- ME preview projects the selected `KeyboardDefinition`, including `keyModifications`,
  original row lengths, `widthMultiplier` spans, icons and directional legends.
  ABC / Shift / Numeric tabs only change local preview state.
- HK thumbnails and `MachineCutSkeleton` consume `MachineCutLayouts.touchMain`;
  symbols and desktop layers also use shared definitions. The older desktop skeleton
  is retained as a separate prototype, never used as the touch thumbnail.
- Thumbnail keys are read-only; the entire selector is an accessible radio option.
- Selection writes only `GemCutPreferences`. Layout/theme settings are observed, not changed.
- HK remains experimental and uses prototype character assignments. Its selection never
  rewrites the saved ME character layout. No claim is made that HK can project ME mappings.

Manual device checks:

1. Open Gem cuts in dark and light mode; select each cut and check selection feedback.
2. Compare ME thumbnail/preview to the active layout, including wide spacebar and controls.
3. Change a character via Modify keys; reopen and confirm its preview legend changes.
4. Switch ABC / Shift / Numeric; confirm the preview updates without opening an IME or crashing.
5. Select HK; try Shift, symbols and the desktop layer; reopen Keywi in a text field.
6. Select ME again; confirm the original layout/theme and modifications are preserved.
7. Check large font scale and TalkBack radio selection; scroll to see the complete preview.

Automated validation: formatting, unit tests (including shared touch topology/spans),
Android lint and debug APK build through Keywi CI. Device visual/interaction checks are
listed separately because a successful build does not establish device UI behavior.
