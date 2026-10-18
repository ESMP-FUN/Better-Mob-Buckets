# Settings

Everything is in `plugins/MobBuckets/config.yml`, with an explanation above
each setting in the file itself. After changing something, run
`/mobbuckets reload`. Use spaces for the indents, never the TAB key.

Which mobs can be caught, and the item for each recipe, is in a file of its
own: [The mob list](mob-list.md).

## catching

| Setting | Default | What it means |
| --- | --- | --- |
| `chance` | `50` | The chance, in percent, that a fresh bucket catches the mob. 50 is one try in two. A miss breaks the bucket. |
| `strengthened-chance` | `75` | The chance for a bucket strengthened in a brewing stand. 75 is three tries in four. |
| `only-owner-catches-pets` | `true` | `true`: only the owner can catch a tamed pet. `false`: anyone can. |
| `protect-other-plugins-mobs` | `true` | `true`: mobs another plugin made, like custom bosses or shop keepers, can't be caught. Leave it on unless you know those mobs are safe to move. |
| `spawner-mobs` | `true` | `true`: mobs from a monster spawner or trial spawner can be caught like any other. `false`: they can't, so a spawner farm stays where it was built. |

## releasing

| Setting | Default | What it means |
| --- | --- | --- |
| `mobs-never-despawn` | `true` | `true`: a mob that was let out never despawns, even a zombie, because a player moved it on purpose. `false`: it behaves like any other mob of its kind. |

## strengthening

Players put a fresh mob bucket where the potion bottles go in a brewing stand,
the item below on top, and fuel it with blaze powder. A bucket can only be
strengthened once.

| Setting | Default | What it means |
| --- | --- | --- |
| `enabled` | `true` | `false`: buckets can't be strengthened. |
| `item` | `redstone` | The item that goes on top of the brewing stand. |

## claims

| Setting | Default | What it means |
| --- | --- | --- |
| `respect-claims` | `true` | With GriefPrevention: inside somebody else's claim, catching an animal, pet or villager needs container trust (`/containertrust`), and letting a mob out needs build trust (`/trust`). Monsters can be caught anywhere. `false`: claims are ignored. |

Players with `mobbuckets.bypass-claims` ignore claims either way.

## crafting

Every bucket is one empty bucket, the extra items below, and some of the mob's
own item from [the mob list](mob-list.md), in any order. A crafting grid fits
9 items, so all of it together can't be more than 9.

| Setting | Default | What it means |
| --- | --- | --- |
| `show-in-recipe-book` | `true` | `true`: every recipe is in the players' recipe books from the start. `false`: they work them out themselves. |
| `normal-mobs.mob-item-amount` | `3` | How many of the mob's item go into a normal bucket. |
| `normal-mobs.extra-items` | 3 slime balls | The other items in a normal bucket. |
| `dangerous-mobs.mob-item-amount` | `3` | How many of the mob's item go into a reinforced bucket. |
| `dangerous-mobs.extra-items` | 3 slime blocks, 2 chains | The other items in a reinforced bucket. |

The extra items are a list of names and amounts:

```yaml
crafting:
  normal-mobs:
    mob-item-amount: 3
    extra-items:
      slime_ball: 3
```

## debug

| Setting | Default | What it means |
| --- | --- | --- |
| `debug` | `false` | `true`: every right-click on a mob with a mob bucket is traced in the console, step by step, including other plugins that stopped it. Use it when catching doesn't work, and switch it off afterwards: it writes a lot. |

## Texts

Every text players see is in `plugins/MobBuckets/messages.yml`, so you can
reword or translate anything. Run `/mobbuckets reload` after a change.
