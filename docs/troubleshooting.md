# Troubleshooting

## Right-clicking a mob does nothing at all

Switch on the trace: set `debug: true` in `config.yml` and run
`/mobbuckets reload`. Right-click the mob again and read the console. It shows
every step:

* whether the bucket was recognised as a mob bucket
* which other plugins saw the click first, and whether one of them **BLOCKED**
  it
* whether it rolled the dice, and at what chance

A line saying another plugin blocked the click points at a claim or
protection plugin, or a region where interacting with mobs is switched off.
Switch `debug` off again afterwards: it writes a lot.

{% hint style="warning" %}
**On Minecraft 26.3, use version 0.2.2 or newer.** Older versions ignored
every right-click on a mob there, without a message and without a debug line.
{% endhint %}

## "This bucket is made for a ..."

Each bucket catches one kind of mob. Check its name: a Cow Bucket only catches
cows. `/mobbuckets recipe <mob>` shows the right recipe.

## A mob can't be caught

The message says why:

* **Somebody else's pet.** Only the owner can catch it.
* **Somebody else's claim.** You need their container trust there.
* **On a lead, with a rider, riding something, or trading.** Free it first.
* **Made by another plugin.** Custom bosses and shop keepers are protected
  (`protect-other-plugins-mobs`).
* **From a spawner**, when the server set `spawner-mobs: false`.
* **On the never list**, like the Warden or ghasts. See
  [The mob list](reference/mob-list.md).

## A mob won't come out

* **No room.** A mob needs space to stand where you clicked. Try an open spot.
* **Somebody else's claim.** Letting a mob out needs build trust there.
* **"This bucket wasn't filled on this server".** The bucket's signature
  doesn't match. That happens when `bucket-key.dat` was deleted or replaced,
  or the bucket was edited or brought from another server. Restore the old
  `bucket-key.dat` from a backup to make the old buckets work again.

## A recipe doesn't show in the recipe book

* `crafting.show-in-recipe-book` may be off.
* Two mobs in `mobs.yml` share an item, so only one of them got a recipe. The
  console names both at startup and after `/mobbuckets reload`. Give every mob
  its own item.
* The recipe has more than 9 items in total: the bucket, the extra items and
  the mob items have to fit one crafting grid.

## Another plugin's recipe ate a mob bucket

It shouldn't: recipes from other plugins that take "any bucket" are blocked
from using mob buckets. If it still happens, the other plugin crafted it
itself. Report it with both plugin names.
