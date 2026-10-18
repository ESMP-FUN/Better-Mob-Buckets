# Commands, permissions & placeholders

## Commands

Everything is under `/mobbuckets`, or `/mobbucket`.

| Command | What it does | Who |
| --- | --- | --- |
| `/mobbuckets recipe <mob>` | Shows what that mob's bucket is crafted from | everyone |
| `/mobbuckets give <player> <mob> [amount] [strengthened]` | Gives mob buckets, for example from a shop or crate plugin | operators |
| `/mobbuckets reload` | Reads `config.yml`, `mobs.yml` and `messages.yml` again | operators |

`give` works from the console too, so shop, crate and vote plugins can hand
out buckets. Add `strengthened` at the end for buckets that already have the
better chance.

## Permissions

| Permission | What it allows | Default |
| --- | --- | --- |
| `mobbuckets.use` | Catching mobs and letting them out | everyone |
| `mobbuckets.recipe` | `/mobbuckets recipe` | everyone |
| `mobbuckets.give` | `/mobbuckets give` | operators |
| `mobbuckets.reload` | `/mobbuckets reload` | operators |
| `mobbuckets.admin` | Both of the above | operators |
| `mobbuckets.bypass-claims` | Catching and letting out mobs inside anyone's claim | nobody |

## Placeholders

With PlaceholderAPI installed, these work in scoreboards, holograms and chat.

| Placeholder | Shows |
| --- | --- |
| `%mobbuckets_caught%` | How many mobs the player has caught |
| `%mobbuckets_missed%` | How many of their buckets broke on a miss |
| `%mobbuckets_released%` | How many mobs they have let out |
| `%mobbuckets_catch_rate%` | Caught out of all tries, in percent |
| `%mobbuckets_chance%` | The catch chance of a fresh bucket, in percent |
| `%mobbuckets_strengthened_chance%` | The catch chance of a strengthened bucket, in percent |

The player numbers show 0 for players who are offline.

## Stats

With **Advanced Player Metrics** installed, mobs caught (with a count per mob),
buckets broken by a miss and mobs let out show in `/stats` and on its
leaderboards. Nothing to set up.
