# For plugin developers

Better Mob Buckets fires four events, in the package
`com.esmpfun.mobbuckets.api`. Add `softdepend: [MobBuckets]` to your
`plugin.yml` and compile against the jar.

| Event | When | Can be cancelled |
| --- | --- | --- |
| `MobCatchEvent` | Before the catch chance is rolled. `chance` can be changed. | Yes: nothing happens and the bucket is kept |
| `MobCatchResultEvent` | After a try, with `caught` true or false | No |
| `MobReleaseEvent` | Before a mob is let out at `location` | Yes: the mob stays in the bucket |
| `MobReleasedEvent` | After the mob is in the world | No |

{% hint style="info" %}
A cancelled event sends the player no message, so send your own if they should
know why.
{% endhint %}

## Examples

Give players with a perk a better chance:

```kotlin
@EventHandler
fun onCatch(event: MobCatchEvent) {
    if (event.player.hasPermission("myserver.luckycatcher")) event.chance += 10
}
```

Stop buckets being emptied in spawn:

```kotlin
@EventHandler
fun onRelease(event: MobReleaseEvent) {
    if (isSpawn(event.location)) {
        event.isCancelled = true
        event.player.sendMessage("You can't let mobs out at spawn.")
    }
}
```

## Giving buckets from another plugin

The simplest way is the command, from the console:

```
mobbuckets give <player> <mob> [amount] [strengthened]
```
