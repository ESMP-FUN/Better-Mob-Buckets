# Install Better Mob Buckets

## Step 1 - Pick the right file

Download it from the
[GitHub page](https://github.com/ESMP-FUN/Better-Mob-Buckets). There are three
files. Use the one that matches your server:

| Your server | The file |
| --- | --- |
| 1.21.1 to 1.21.x | `MobBuckets-<version>.jar` |
| 26.1 and 26.2 | `MobBuckets-<version>-mc26.jar` |
| 26.3 and newer | `MobBuckets-<version>-mc263.jar` |

## Step 2 - Put it on the server

Drop that one file into your server's `plugins/` folder and restart. That is
the whole installation.

On the first start it makes `plugins/MobBuckets/`, with:

* `config.yml`, the catch chances, recipes and rules, explained in the file
* `mobs.yml`, which mobs can be caught and what each bucket is crafted from
* `messages.yml`, every text players see, so you can reword or translate it
* `bucket-key.dat`, see below

## Step 3 - Try it

Check the recipe for a cow:

```
/mobbuckets recipe cow
```

Craft one, right-click a cow, and try again if it misses. Right-click a block
to let the cow out. Or skip the crafting:

```
/mobbuckets give YourName cow 1
```

{% hint style="success" %}
That is all most servers need. Every player can craft, catch and let out
mobs straight away, and every recipe is in their recipe book. Only change
[the settings](../reference/settings.md) if you want different odds or
recipes.
{% endhint %}

## Keep bucket-key.dat

{% hint style="danger" %}
**Never delete `plugins/MobBuckets/bucket-key.dat`, and copy it along when you
move the server.** Every bucket with a mob in it is signed with it, so nobody
can make a fake bucket in creative mode and let out whatever they like. Without
the file, the mobs already in buckets can't be let out any more.
{% endhint %}

## Claims

With **GriefPrevention** installed, claims are respected without any setup.
Inside somebody else's claim a player needs container trust to catch animals,
pets or villagers there, and build trust to let a mob out. Monsters can be
caught anywhere. Switch it off with `claims.respect-claims: false`.
