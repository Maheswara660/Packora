# Contributing

Thank you for contributing to Packora! Packora is free and open-source software licensed under the **[GNU General Public License v3.0 (GPLv3)](https://github.com/Maheswara660/Packora/blob/main/LICENSE)**.

## Contribution lanes

| Lane | What you do | Guide |
| --- | --- | --- |
| Issues | Report a bug or request a feature | [GitHub Issues](https://github.com/Maheswara660/Packora/issues) |
| Code | Fix a bug or build a feature in Android `:app` or `:template` | Fork repo & open a Pull Request |
| Docs | Improve this documentation site | Edit under `docs/` and open a PR |

## Ground rules

- **License Compliance**: All contributions are licensed under the GNU General Public License v3.0 (GPLv3).
- **Code Quality**: Match the patterns, clean Kotlin styling, and architectural conventions established in the surrounding code.
- **Privacy & Security**: Never commit secret keys, passwords, keystore files, or sensitive credentials.
- **English Communication**: Pull requests, commit messages, and issues should be written in English.

## Before you open a PR

- Rebuild the template APK and ensure it packages cleanly:
  ```bash
  ./gradlew :template:assembleRelease :app:copyTemplateApk
  ```
- Verify compilation passes with zero errors:
  ```bash
  ./gradlew :app:compileDebugKotlin :template:compileReleaseKotlin
  ```
- Run unit tests:
  ```bash
  ./gradlew testDebugUnitTest
  ```
