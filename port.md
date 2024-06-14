# Minecraft Code Breaking Changes
### 1.19.4
Initial Release

### 1.20.?
#### Worked Around:
- `ServerPlayerEntity`'s override of `getWorld` was removed: Cast to `PlayerEntity` before calling.

### 1.20.5
#### Possible workaround:
- `PlayerEntity::playSound` was renamed to `plaSoundToPlayer` (Yarn mapping changes). Doesn't need recompilation.

### 1.21.0
#### No Workaround
- `isLeashed()` was moved from `MobEntity` to an interface. No code change required, but needs recompilation.
