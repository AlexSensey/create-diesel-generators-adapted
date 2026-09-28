<div align="center"><img width="512" height="153" alt="CREATE_DIESEL_GENERATORS" src="https://github.com/user-attachments/assets/ca4378d3-9bd5-45bf-9629-fd5a28170767" /></div>
<hr>
<div align="center">
<a href="https://github.com/AlexSensey/create-diesel-generators-adapted/releases/latest"><img src="https://img.shields.io/github/v/release/AlexSensey/create-diesel-generators-adapted?logo=github&label=release"></a>
<a href="https://github.com/AlexSensey/create-diesel-generators-adapted/releases"><img src="https://img.shields.io/github/downloads/AlexSensey/create-diesel-generators-adapted/total?logo=github&label=downloads"></a>
<img src="https://img.shields.io/badge/Minecraft-26.3-62b47a">
<img src="https://img.shields.io/badge/NeoForge-26.3-orange">
  <br>
</div>

An unofficial Minecraft 26.3 NeoForge adaptation of Create: Diesel Generators.
It adds diesel engines, oil processing, and industrial machinery to
[Create: Adapted](https://www.curseforge.com/minecraft/mc-mods/create-adapted).

## Development branches

- `mc26.2-adapted` retains the Minecraft 26.2 adaptation.
- `mc26.3-adapted` contains this Minecraft 26.3 port.
- The 26.3 build uses local Create: Adapted 1.19 artifacts configured in `gradle.properties`.

## Local 26.3 candidate

Build with Java 25: `gradlew.bat build -x test`.
The candidate is `build/libs/createdieselgenerators-26.3-1.3.15-adapted-0.607.jar`.
Compilation and the existing server class audit pass; startup and gameplay are pending in Prism 26.3.

## Download

Download the latest adapted JAR from [GitHub Releases](https://github.com/AlexSensey/create-diesel-generators-adapted/releases/latest).

- Requires Minecraft 26.3 and NeoForge 26.3.0.25-beta or newer.
- Requires [Create: Adapted 1.19 or newer](https://www.curseforge.com/minecraft/mc-mods/create-adapted).
- Install the same Create Diesel Generators: Adapted JAR on both the client and server.

## About this adaptation

This adaptation is maintained by Buggsify and is not affiliated with the official
Create or Create: Diesel Generators development teams.

The adaptation includes Flywheel-compatible rendering, updated fluid and recipe APIs,
working engines and processing machines, and JEI 30 categories for Diesel-specific
fermenting, casting, molding, distillation, hammering, and wire-cutting recipes.

See [ADAPTED-CHANGELOG.md](ADAPTED-CHANGELOG.md) for adaptation-specific changes.

## Original project and credits

Create: Diesel Generators was created by George VI. The original project remains
available on [GitHub](https://github.com/george8188625/Create-Diesel-Generators),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-diesel-generators),
and [Modrinth](https://modrinth.com/mod/create-diesel-generators).

This repository preserves the original project's MIT license and attribution.

### Overview
The main focus of this mod is to provide a streamlined implementation of industrial-like features to the create mod while maintaining its style.

While this is not gonna give you the absolute most realistic implementation, this provides something simple. You don't have to overthink anything when playing. Just play.

If you want to contribute, feel free to open a PR (to the default branch), whether to fix something or add a feature.

If you want to help translate this mod, make a pull request with the translation files<br>
