# M3E Widgets

Two Material 3 Expressive-style home screen widgets built with Jetpack Glance:

- **Weather**: current temp, feels-like, condition, high/low (Open-Meteo, no API key)
- **Health**: steps (with progress bar), latest heart rate, last night's sleep (Health Connect)

Both have a tap-to-refresh pill, use your wallpaper's dynamic colors, and sync every 30 minutes.

## Run it
1. Open this folder in Android Studio (Ladybug or newer). Accept the Gradle sync prompt.
2. Run on a phone with Android 12+ (minSdk 31).
3. In the app, tap **Allow location** and **Allow Health Connect**, then **Sync now**.
4. Long-press the home screen, Widgets, M3E Widgets, and add both.

## Notes
- Pixel Weather has no public API, so weather comes from Open-Meteo instead.
- Google Health / Fitbit data reaches this app through Health Connect. In the Fitbit / Google Health app, make sure Health Connect sync is on.
- Heart rate and sleep in the background need the "read in background" Health Connect permission, which is requested with the others.
- Default location (used until a fix is cached) is Cairo: change `DEFAULT_LAT/LON` in `Weather.kt`.
- Step goal is 10,000: change `STEP_GOAL` in `HealthWidget.kt`.
- Glance doesn't have full M3E components yet, so the expressive look is done with large radii, tonal containers, bold type and pill buttons.
