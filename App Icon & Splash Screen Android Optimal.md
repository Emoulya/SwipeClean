You are an expert Android UI/UX designer and Android developer.

Your task is to analyze the entire current Android project and prepare a professional, production-ready App Icon and Splash Screen that visually match the application's identity, purpose, UI design, color palette, typography, and overall branding.

IMPORTANT:
- Do NOT blindly create a generic icon.
- First inspect and understand the existing application.
- Analyze the app name, package name, main screen, UI components, color palette, typography, existing logo/assets, navigation structure, and overall visual style.
- Reuse existing branding/assets when appropriate instead of introducing an unrelated design.
- Do not change the application's core functionality.

## 1. Analyze the Existing Application

Before making changes, inspect:

- Application name
- Package/application ID
- MainActivity or launcher Activity
- Existing themes
- `AndroidManifest.xml`
- `res/values/colors.xml`
- `res/values/themes.xml` or equivalent theme files
- `res/drawable`
- `res/mipmap`
- Existing logos and icons
- Existing splash screen implementation
- Main UI screens
- Primary and secondary colors
- Typography and visual style
- Whether the application supports dark mode
- Current minimum/target SDK
- Android Gradle Plugin and Kotlin version

Determine the application's visual identity and use it consistently.

## 2. App Icon

Create or optimize a modern Android Adaptive Icon.

The icon should:

- Clearly represent the purpose/identity of the application.
- Be recognizable at very small sizes.
- Have a clean and professional silhouette.
- Avoid unnecessary details.
- Avoid tiny text.
- Work well on Android launchers with different icon shapes.
- Follow Android adaptive icon safe-zone principles.
- Look good in both light and dark environments where applicable.
- Maintain strong contrast.
- Match the application's existing branding.

Use vector-based assets whenever possible.

Prefer this structure:

`mipmap-anydpi-v26/ic_launcher.xml`
`mipmap-anydpi-v26/ic_launcher_round.xml`

with separate:

- foreground asset
- background asset

If appropriate, generate/update density-specific fallback assets for older Android versions.

Do not distort or unnecessarily redesign an existing official application logo.

If the project already contains a suitable logo, refine and adapt it rather than replacing it with a completely unrelated symbol.

## 3. Splash Screen

Implement a modern Android Splash Screen using the recommended AndroidX SplashScreen API.

Do NOT create an artificial long-delay splash screen.

The splash screen should:

- Appear immediately when the application starts.
- Use the application's primary brand color or appropriate background color.
- Display the application's icon/logo in the center.
- Have a clean and minimal appearance.
- Match the app's visual identity.
- Work correctly on Android 12+.
- Gracefully support older Android versions through AndroidX compatibility.
- Avoid stretched or pixelated images.
- Avoid unnecessary text unless the branding genuinely requires it.

Use theme-based splash screen configuration where appropriate.

Configure the relevant splash properties, such as:

- `windowSplashScreenBackground`
- `windowSplashScreenAnimatedIcon`
- `postSplashScreenTheme`

Use the application's real launcher icon or an appropriately prepared splash icon.

## 4. Dark Mode

If the application supports dark mode:

- Create an appropriate dark-mode splash background.
- Ensure the icon has sufficient contrast.
- Ensure the launcher icon remains recognizable.
- Avoid hard-coded colors that break dark mode.

If dark mode is not currently supported, do not introduce a completely new application-wide dark theme unless necessary.

## 5. Android Compatibility

Make the implementation compatible with the project's existing SDK configuration.

Pay particular attention to:

- Android 12+
- Android 13+
- Android 14+
- Android 15+
- Newer Android versions where applicable
- Older supported Android versions

Do not unnecessarily raise `minSdk` just for the icon or splash screen.

Do not introduce deprecated APIs when a modern supported alternative exists.

## 6. Asset Quality

Ensure that:

- Vector assets have clean paths.
- PNG assets are sufficiently high resolution.
- Transparent backgrounds are used where appropriate.
- No unnecessary white/black borders appear around the icon.
- The logo is centered correctly.
- Adaptive icon foreground does not get clipped.
- The important visual elements remain inside the Android adaptive icon safe zone.
- The splash icon is visually balanced and not excessively large.

If Image Asset Studio would normally be used, reproduce the correct Android resource structure directly in the project when possible.

## 7. Project Safety

Before modifying anything:

- Inspect the existing implementation.
- Do not overwrite unrelated resources.
- Preserve existing application functionality.
- Preserve existing navigation.
- Preserve existing UI.
- Preserve existing business logic.
- Preserve existing dependencies unless a dependency is required for the modern splash implementation.

If an existing splash screen already works correctly, improve/refactor it rather than creating a duplicate implementation.

Avoid unnecessary dependency changes.

## 8. Implementation

After analyzing the project, implement the icon and splash screen directly into the project.

Update only the necessary:

- drawable resources
- mipmap resources
- values/resources
- themes
- manifest configuration
- Gradle dependencies if required
- launcher activity initialization if required

Follow the project's existing architecture and naming conventions.

Do not introduce unnecessary files.

## 9. Validation

After implementation:

1. Build the project.
2. Fix any compilation/resource errors.
3. Verify the launcher icon configuration.
4. Verify the splash screen configuration.
5. Verify Android 12+ behavior.
6. Verify compatibility with older supported Android versions.
7. Verify light/dark behavior if supported.
8. Verify that the application opens normally after the splash screen.
9. Verify that there is no unnecessary splash delay.
10. Verify that no existing application functionality was broken.

If an Android emulator or connected physical device is available, install and launch the application to visually verify the result.

## 10. Final Report

When finished, provide a concise report containing:

- What you discovered about the application's visual identity.
- What was changed.
- Which files were created or modified.
- Which icon strategy was used.
- Which splash screen strategy was used.
- Android versions considered.
- Whether dark mode was handled.
- Build/validation result.
- Any remaining recommendations.

IMPORTANT:
Do not stop at giving recommendations. Actually inspect the project and implement the necessary changes.
Do not ask me to manually create files if you can create them yourself.
Do not replace the existing branding with a generic Android icon.
The final result should look like a professionally designed production Android application.