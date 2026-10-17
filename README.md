# Mob Buckets

Craft a bucket for one kind of mob, scoop the mob up, and let it out somewhere else. Like catching a fish in a water bucket, but for cows, villagers, horses, zombies and almost everything else.

## How it works

1. **Craft a bucket for the mob you want.** Every mob bucket is an empty bucket, 3 slime balls and 3 of an item that belongs to that mob. Put them anywhere in the crafting grid, whole stacks are fine, and shift-click to craft as many as you can afford. A Zombie Bucket takes rotten flesh, a Wolf Bucket takes bones, a Villager Bucket takes emeralds. Every recipe is in the recipe book.
2. **Right-click the mob with it.** It works half the time. When it misses, the bucket breaks and the mob stays where it is.
3. **Right-click a block to let the mob out.** You're left with a normal empty bucket, ready for the next recipe.

Want better odds? Put a fresh mob bucket in a brewing stand where the bottles go, with redstone on top. The strengthened bucket works three times out of four. A bucket can only be strengthened once.

Some mobs are **dangerous** (blazes, breezes, wither skeletons, shulkers, evokers and a few more). Their buckets also need slime blocks and chains. Bosses and a few others (the Wither, the Ender Dragon, wardens, guardians, ghasts) can't be caught at all.

Empty mob buckets stack up to 16 of the same kind. A bucket with a mob in it doesn't stack.

## What the mob keeps

Everything: its name, whether it's a baby, its health, colour and pattern, who tamed it, a horse's speed and jump, a villager's job, level and trades. Items in a donkey, mule or llama chest drop on the ground when it is caught, so a bucket never carries items around.

## Fair play

- Only the owner of a tamed pet can catch it.
- With GriefPrevention installed, catching an animal or villager inside someone else's claim needs container trust there, and letting a mob out needs build trust. Monsters can be caught anywhere.
- Mobs from other plugins (custom bosses, shop keepers) can't be caught.
- Optionally, mobs from monster spawners can't be caught either (`spawner-mobs` in `config.yml`), so spawner farms stay where they were built.
- A mob that is on a lead, carrying a rider or riding something has to be freed first.

## Installing

1. Pick the jar for your server version: `MobBuckets-<version>.jar` for 1.21.x, `-mc26.jar` for 26.0 to 26.2, `-mc263.jar` for 26.3 and newer.
2. Put it in your `plugins` folder and restart.
3. Change anything you like in `plugins/MobBuckets/config.yml` (chances, recipes), `mobs.yml` (which mobs, which items) and `messages.yml` (every text players see), then run `/mobbuckets reload`.

Needs Paper (or a fork of it). Doesn't work on Spigot.

**Keep `plugins/MobBuckets/bucket-key.dat`.** It is made on the first start, and every bucket with a mob in it is signed with it, so nobody can make a fake bucket in creative mode and release whatever they like. If the file is deleted, mobs already in buckets can't be let out any more. On a network where players carry items between servers, copy the same file to every server.

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
