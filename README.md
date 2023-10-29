# Allay, behave !

An allay Quality of Life mod.

Some features can be fine-tuned in the gamerules.

## Features

### Allay calling:
Allays who like you can be called to your side, either by crouching while looking at them, or by typing their name in the chat.

Called allays will follow you closely for a few second, or so long as you maintain eye contact. For that duration, they will remain close even if you take their item away. They will forget their liked note-block, and won't be distracted by nearby items or note-blocks.

Calling an allay by sight requires precisely positioning the crosshair on the allay, but maintaining eye contact afterward is easier. You'll only need to look into their general direction, so you can easily keep multiple allays at your heels.

Once you're done with them, you can send them off early using LMB.

### Teleportation

This is much more limited than tameable animals' teleportation; allays will only attempt to teleport to players they are actively trying to reach.

In particular, allays _will not_ teleport if:
- They are bound to a note-block.
- They are trying to gather items.
- They are considerably far away (>64 blocks). This point prevents allays from teleporting across the entire world. After a note-block malfunction for example.

Allays are not able to teleport across dimensions at this time.

### Other tweaks:
- **Allays can be pushed around by their liked player using LMB.** This helps removing them from places where you're trying to build.
- (_Requires clientside_) **Allays can no longer push other players around when colliding.** This prevents them from pushing you off the edge of a block, or offsetting your croshair right as you're about to interact with something.
- **Allays will aim a little higher when giving items to a player.** This prevents them from trying to give you items through the floor.
