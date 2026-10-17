# Mob Buckets

Craft a bucket for each kind of mob, scoop the mob up, and let it out somewhere else. <br>
Like catching a fish in a water bucket, but for cows, villagers, horses, zombies and almost everything else.

## How it works

1. **Crafting Recipe** has the same base: 1x empty bucket and 3x slime balls <br>
+ 3 of an item that matches the mob. <br>
Any crafting grid position/layout works, whole stacks are fine, and shift-click to craft in bulk. <br>
Every recipe is in the recipe book.
2. **Right-click mob** to try and *bucket* a mob at 50% chance by default. <br>
If collecting fails, your bucket breaks and is removed from your inventory.
3. **Right-click on a block** to release the collected mob. You're left with a normal empty bucket, ready for the next recipe.

For better odds (+25%), put a crafted (empty) mob bucket in a brewing stand and power it up with redstone dust. A bucket can only be powered up once.

Some **dangerous** mobs (blazes, breezes, wither skeletons, shulkers, evokers, etc.) require reinforced buckets, which is done by adding slime blocks and chains. 

The Wither, Ender Dragon, Warden, Guardian and Ghasts cannot be captured.

Empty mob buckets stack up to 16 of the same kind. A bucket with a mob in it doesn't stack.

## What the mob keeps

Everything: its name, whether it's a baby, its health, colour and pattern, who tamed it, a horse's speed and jump, a villager's job, level and trades. 

## What the mob does not keep

Items in a donkey, mule or llama chest drop on the ground when it is caught, so a bucket never carries items around.

## Fair play

- Only the owner of a tamed pet can catch it.
- With GriefPrevention installed, catching an animal or villager inside someone else's claim needs container trust there, and letting a mob out needs build trust. Monsters can be caught anywhere.
- Mobs from other plugins (custom bosses, shop keepers) can't be caught (yet?).
- Optionally, mobs from monster spawners can't be caught either (`spawner-mobs` in `config.yml`), so spawner farms stay where they were built.
- A mob that is on a lead, carrying a rider or riding something has to be free first.

## Installing

1. Pick the jar for your server version: <br>
`MobBuckets-<version>.jar` for 1.21.x, <br>
`-mc26.jar` for 26.0 to 26.2, <br>
`-mc263.jar` for 26.3 and newer.
2. Put it in your `plugins` folder and restart.
3. Change anything you like, in <br>
`plugins/MobBuckets/config.yml` (chances, recipes), <br>
`mobs.yml` (which mobs, which items), and <br>
`messages.yml` (every text players see), <br>
then run `/mobbuckets reload`.

Needs Paper (or a fork of it). Doesn't work on Spigot.

:warning: **Keep `plugins/MobBuckets/bucket-key.dat`.** :warning: <br>
It is made on the first start, and every bucket with a mob in it is signed with it, so nobody can make a fake bucket in creative mode and release whatever they like. If the file is deleted, mobs already in buckets can't be let out any more. On a network where players carry items between servers, copy the same file to every server.

## Commands

| Command | What it does | Permission | Who has it |
|---|---|---|---|
| `/mobbuckets recipe <mob>` | Shows what a mob bucket is crafted from | `mobbuckets.recipe` | everyone |
| `/mobbuckets give <player> <mob> [amount] [strengthened]` | Gives mob buckets, for example from a shop or crate plugin | `mobbuckets.give` | ops |
| `/mobbuckets reload` | Reloads all three settings files | `mobbuckets.reload` | ops |

Other permissions: `mobbuckets.use` (catch and let out mobs, everyone), `mobbuckets.bypass-claims` (ignore claims, nobody by default), `mobbuckets.admin` (give and reload, ops).

## Works with

All optional; Mob Buckets runs fine without them.

- **GriefPrevention:** claims are respected, see Fair play.
- **Advanced Player Metrics:** mobs caught (with a breakdown per mob), buckets broken by a miss, and mobs let out show in `/stats` and on its leaderboards.
- **PlaceholderAPI:** for scoreboards, holograms and chat.

| Placeholder | Shows |
|---|---|
| `%mobbuckets_caught%` | How many mobs the player has caught |
| `%mobbuckets_missed%` | How many buckets broke on a miss |
| `%mobbuckets_released%` | How many mobs the player has let out |
| `%mobbuckets_catch_rate%` | Caught out of all tries, in percent |
| `%mobbuckets_chance%` | The catch chance of a fresh bucket, in percent |
| `%mobbuckets_strengthened_chance%` | The catch chance of a strengthened bucket, in percent |

The player numbers count from this version on and show 0 for players who are offline.

## For plugin developers

Mob Buckets fires four events (package `com.esmpfun.mobbuckets.api`):

| Event | When | Can be cancelled |
|---|---|---|
| `MobCatchEvent` | Before the catch chance is rolled. `chance` can be changed. | Yes, the bucket is kept |
| `MobCatchResultEvent` | After a try, `caught` true or false | No |
| `MobReleaseEvent` | Before a mob is let out at `location` | Yes, the mob stays in the bucket |
| `MobReleasedEvent` | After the mob is in the world | No |

A cancelled event sends the player no message, so send your own. Add `softdepend: [MobBuckets]` and compile against the jar.

## Building

```bash
./gradlew build                    # 1.21.x
./gradlew build -PmcVariant=26     # 26.0 to 26.2
./gradlew build -PmcVariant=263    # 26.3 and newer
```

The jar lands in `build/libs/`.

## License

Source-available: free to use, no redistribution (see [LICENSE](LICENSE)). Made with Kotlin by [darkstarworks](https://github.com/darkstarworks).
