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
