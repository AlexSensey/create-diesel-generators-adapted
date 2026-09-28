## 26.3-1.3.15-adapted-0.607

- Fixed a ConcurrentModificationException in the reverse loot index during parallel loot table loading in Minecraft 26.3. This prevented the world from opening with a registry loading error.
- Uses atomic updates to a concurrent map and immutable lists, avoiding concurrent list mutation and duplicate entity entries.
- The Prism log from 2026-09-27 21:31 shows that 0.606 failed to load the world; the earlier initial manual impression did not establish successful world loading.
- Validation: build and existing dedicated-server class audit; a new Prism world-loading check is pending.

## 26.3-1.3.15-adapted-0.606

- Ported to Minecraft 26.3 / NeoForge 26.3.0.25-beta on the separate `mc26.3-adapted` branch. The `mc26.2-adapted` branch is preserved.
- Targets Create: Adapted 1.19 and its isolated Flywheel, fluid and item compatibility APIs; updates JEI and CC:Tweaked compile APIs.
- Updates datapack registry registration, fuel components, item rendering, redstone connections, inventory returns and multiblock placement data for 26.3.
- Rebuilds oil noise with the original octave seeds, frequencies and weights using the new noise API; numerical parity across Minecraft versions has not been tested.
- Disables the optional Strut Your Stuff Flywheel visual registration because its published visual uses the upstream Flywheel namespace. Its fallback renderer remains; this optional integration is untested.
- Validation: `gradlew.bat build -x test --offline` passed, including the existing dedicated-server class audit. No game launch or gameplay tests were run; Prism 26.3 validation remains pending.

## 26.2-1.3.15-adapted-0.605

Metadata-only compatibility release for Create: Adapted 1.x. Adds the [1.0,2.0) version range while retaining [6.0.11-adapted-0.987,). Runtime classes and assets are unchanged from 26.2-1.3.15-adapted-0.604. Startup/gameplay validation is still required.

# Create Diesel Generators: Adapted changelog

## 26.2-1.3.15-adapted-0.6

- Separated client rendering, previews, screens, and input handlers from shared server logic.
- Fixed dedicated-server class loading for menus, track placement, pumpjacks, engines, tools, and events.
- Fixed optional Strut Your Stuff registration loading client-only classes on a server.
- Added an automatic dedicated-server bytecode audit to release builds.
- Updated and tested against Create: Adapted 0.98.

## 26.2-1.3.15-adapted-0.5

- Preserved every language file from the original 1.21.1 mod.
- Corrected the original German filename from de_DE.json to de_de.json so
  Minecraft recognizes the translation.
- Added item-name aliases for translated blocks in JEI and inventories.

## 26.2-1.3.15-adapted-0.4

- Fixed custom Diesel recipe categories disappearing from JEI on dedicated servers.
- The server now synchronizes Basin Fermenting, Bulk Fermenting, Compression
  Molding, Casting, Distillation, Hammering, and Wire Cutting recipes to clients.

## 26.2-1.3.15-adapted-0.3

- Fixed Diesel JEI categories receiving an empty client recipe cache in singleplayer.
- Restored visible JEI tabs and recipes for Basin Fermenting, Bulk Fermenting,
  Compression Molding, Casting, Distillation, Hammering, and Wire Cutting.
- Uses the integrated server recipe manager, matching Create: Adapted behavior.

## 26.2-1.3.15-adapted-0.2

- Completed the initial Minecraft 26.2 NeoForge gameplay and rendering adaptation.
- Added Flywheel-compatible moving parts for engines, pumpjacks, turrets, and machines.
- Fixed Huge Diesel Engine rendering and cleanup, fluid processing, contraptions,
  Chemical Turret behavior, recipes, models, textures, and localization.
