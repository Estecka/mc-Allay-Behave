# Minecraft Code Breaking Changes
## 1.19.4
Initial Release

## 1.20.?
### Worked Around:
- `ServerPlayerEntity`'s bad override of `getWorld` was removed: Cast to `PlayerEntity` before calling.

## 1.21.0
### No Workaround:
- `isLeashed()` was moved from `MobEntity` to an interface. No code change required, but needs recompilation.

## 1.21.2
### Worked around:
- `World.getGamerules()` was moved to `ServerWorld`; use `Entity.getServer().getGameRules()` instead.
- `Entity.damage()` takes an extra parameter in first position: provide multiple `methods` to `@inject`, and get arguments with `@Local`
