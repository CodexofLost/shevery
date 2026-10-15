# Stealth Edition & Compatibility Stubs Guide

Some apps and games scan installed package names for `com.hamondev.shevery`, `moe.shizuku.privileged.api`, or `com.rosan.dhizuku` and refuse to run. Conversely, some legacy Shizuku/Dhizuku client apps hardcode package-existence checks for `moe.shizuku.privileged.api` or `com.rosan.dhizuku`.

Shevery solves both problems from **Settings → Application** through the **On-Device Stealth APK Generator** and **Compatibility Stubs**.

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Stealth-and-Stubs

---

## Part 1: Stealth Edition (On-Device Self-Cloning Random Package Generator)

Available on Android 10+ (API 29+) under **Settings → Application → Stealth version**.

### How It Works

1. Instead of embedding a duplicate APK inside `assets/` (which triggers VirusTotal `APK-in-APK` / `Dropper` heuristics and doubles the download size), Shevery clones its own installed base APK (`context.applicationInfo.sourceDir`) directly on-device.
2. When you open **Stealth version**, Shevery generates a fresh random 20-character package name (`com.<8chars>.<7chars>`, e.g., `com.k8m2x9qp.r4v7n1t`) using `SecureRandom` — matching the exact byte length of `com.hamondev.shevery`.
3. You can customize:
   - **App name** (launcher label, default `System Toolkit`, up to 32 characters)
   - **File name** (default `SystemToolkit-v<version>.apk`)
4. When you tap **Save**, `StealthApkGenerator`:
   - Rewrites all occurrences of the current 20-character package ID across `AndroidManifest.xml`, `resources.arsc`, `classes*.dex`, `assets/rish_shizuku.dex`, and `lib/*/libshizuku.so` in both **ASCII/UTF-8** and **UTF-16LE** encodings while keeping binary string-pool offsets intact and preserving internal `.tasker` class names (`com.hamondev.shevery.tasker.*`).
   - Rewrites the launcher label in-place inside the binary `AndroidManifest.xml` StringPool (`SheveryStealthLabelPlaceholder32`) and switches `<application android:label>` from `@string/app_name` (`TYPE_REFERENCE`) to `TYPE_STRING` so your custom app name appears in the launcher without shifting `resources.arsc` offsets.
   - Recalculates the **SHA-1 signature** and **Adler32 checksum** in every modified DEX header (`classes*.dex` and `assets/rish_shizuku.dex`) so Android's package verifier accepts the rewritten bytecode.
   - Preserves nested asset APKs (`shevery-stub.apk`, `shevery-dhizuku-stub.apk`) untouched so their signatures remain valid.
   - Aligns uncompressed ZIP entries (`resources.arsc` to 4 bytes, `.so` native libraries to 4096 bytes) to satisfy Android 11+ (API 30+) `INSTALL_PARSE_FAILED_RESOURCES_ARSC_COMPRESSED` requirements.
   - Signs the rebuilt APK on-device using a freshly generated 2048-bit RSA keypair and self-signed X.509 certificate with both **v1 JAR signing** and **APK Signature Scheme v2** (`APK Sig Block 42`, ID `0x7109871a`, 4096-byte page-aligned) for Android 11–17 compatibility.
   - Saves the finished APK directly to your device's **`Downloads/`** folder via `MediaStore.Downloads`.

### Installing the Stealth APK

> [!IMPORTANT]
> Because `ServerConstants.MANAGER_APPLICATION_ID`, `libshizuku.so`, and all provider authorities (`<pkg>.shizuku`, `<pkg>.fileprovider`, `<pkg>.dhizuku.server`) are rewritten to your new random package ID, the Stealth APK runs with zero static `com.hamondev.shevery` package identifiers and can even be installed alongside or in place of the standard build. If you use legacy apps that check for `moe.shizuku.privileged.api`, enable the **Shizuku Stub** after installing.

Tip: Use **Settings → Backups → Backup** before switching to the Stealth APK, then restore your settings, authorized apps, and modules in one tap after installing the stealth copy.

---

## Part 2: Compatibility Stubs

Available under **Settings → Application → Compatibility Stubs** (`CompatStubsScreen`).

### Available Stubs

| Stub | Package Name | Asset File | Purpose |
|---|---|---|---|
| **Shizuku Stub** | `moe.shizuku.privileged.api` | `shevery-stub.apk` | Satisfies apps that check if the original Shizuku Manager package is installed and forwards permission/binder intents to Shevery. |
| **Dhizuku Stub** | `com.rosan.dhizuku` | `shevery-dhizuku-stub.apk` | Satisfies apps that check if the official Dhizuku package is installed when Shevery acts as the Device Owner / Dhizuku provider. |

### Multi-Channel Installation Pipeline

When you tap **Install** or **Uninstall** on a stub card, `StubManager` automatically tries available privileged channels in order until one succeeds:

1. **Device Owner / Profile Owner**: Uses Android's `PackageInstaller` session API (`--bypass-low-target-sdk-block` enabled automatically on Android 14 / API 34+).
2. **Shevery Server**: Streams the APK to `/data/local/tmp/` and runs `pm install -r -d -t -g` through the active Shizuku binder.
3. **Root (`libsu`)**: Installs directly from private storage via a root shell.
4. **Local ADB**: Connects to the local ADB daemon (`127.0.0.1` on the live ADB port, configured TCP port, or `5555`) and installs from external app storage.

> **Biometric Protection**: Installing or removing stubs can be guarded with biometric or device credential authentication via **Settings → Security → Installing or removing stubs** (`ProtectedAction.STUB_MANAGEMENT`).

---

## Source Files

- `manager/src/main/java/moe/shizuku/manager/stealth/StealthApkGenerator.kt`
- `manager/src/main/java/moe/shizuku/manager/compat/StubManager.kt`
- `manager/src/main/java/moe/shizuku/manager/settings/CompatStubsScreen.kt`
