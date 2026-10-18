# The mob list

`plugins/MobBuckets/mobs.yml` decides which mobs can be caught and what goes
into each bucket. Each line is a mob, then the item for its recipe:

```yaml
normal-mobs:
  cow: leather
  zombie: rotten_flesh
```

`zombie: rotten_flesh` means a Zombie Bucket is an empty bucket, the extra
items from `config.yml` (3 slime balls), and 3 rotten flesh. Run
`/mobbuckets reload` after changing the file.

## The three lists

| List | What it means |
| --- | --- |
| `normal-mobs` | Caught with a normal bucket: the extra items under `crafting.normal-mobs` |
| `dangerous-mobs` | Need a reinforced bucket: the extra items under `crafting.dangerous-mobs` (3 slime blocks and 2 chains) |
| `cannot-be-caught` | Never, whatever bucket. Right-clicking one says so. |

## Changing it

* **Stop a mob being caught.** Remove its line, or move it to
  `cannot-be-caught`.
* **Change its recipe.** Change the item. Give every mob a **different**
  item: two mobs with the same item would have the same recipe, and only one
  of them could be crafted.
* **One bucket for several mobs.** List them together with commas, in quotes.
  The bucket is named after the first one:

  ```yaml
  "llama, trader_llama": "#wool_carpets"
  ```

* **A group of items.** An item that starts with `#` is a group, for example
  `"#wool_carpets"` is a carpet of any colour. Keep the quotes.

Mob and item names are the ones Minecraft uses in commands, like
`/summon zombie` or `/give @s rotten_flesh`. A mob that doesn't exist in your
Minecraft version is skipped.

{% hint style="info" %}
Fish, axolotls, tadpoles and sulfur cubes are left out on purpose: the normal
water bucket already catches them.
{% endhint %}

## What ships with it

**Dangerous mobs:** blaze (blaze rod), breeze (breeze rod), wither skeleton
(coal block), shulker (shulker shell), hoglin (crimson fungus), zoglin (nether
wart block), piglin brute (golden axe), ravager (saddle), evoker (totem of
undying).

**Never caught:** ghast, happy ghast, guardian, elder guardian, the Wither, the
Ender Dragon, the Warden, creaking, vex, silverfish, endermite, giant,
illusioner.

**Everything else** is a normal mob: about 60, from farm animals and horses to
villagers, golems and most monsters. `/mobbuckets recipe <mob>` shows any of
them, or open `mobs.yml` for the full list.
