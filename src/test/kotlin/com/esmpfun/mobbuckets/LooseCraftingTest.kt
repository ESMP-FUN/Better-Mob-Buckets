package com.esmpfun.mobbuckets

import com.esmpfun.mobbuckets.recipe.allocate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class LooseCraftingTest {

    private data class Stack(val item: String, val amount: Int)

    private fun need(item: String, amount: Int): Pair<(Stack) -> Boolean, Int> = { s: Stack -> s.item == item } to amount

    private val zombie = listOf(need("bucket", 1), need("slime_ball", 3), need("rotten_flesh", 3))

    private fun grid(vararg slots: Stack?) = List(9) { slots.getOrNull(it) }

    private fun take(slots: List<Stack?>, needs: List<Pair<(Stack) -> Boolean, Int>> = zombie) =
        allocate(slots, Stack::amount, needs)

    @Test
    fun `full stacks side by side craft and only use what the recipe needs`() {
        val slots = grid(null, null, null, Stack("slime_ball", 58), Stack("bucket", 1), Stack("rotten_flesh", 12))
        assertContentEquals(intArrayOf(0, 0, 0, 3, 1, 3, 0, 0, 0), take(slots))
    }

    @Test
    fun `ingredients split over several slots add up`() {
        val slots = grid(Stack("slime_ball", 1), Stack("slime_ball", 2), Stack("bucket", 1), Stack("rotten_flesh", 2), Stack("rotten_flesh", 5))
        assertContentEquals(intArrayOf(1, 2, 1, 2, 1, 0, 0, 0, 0), take(slots))
    }

    @Test
    fun `too few of an ingredient does not craft`() {
        assertNull(take(grid(Stack("slime_ball", 2), Stack("bucket", 1), Stack("rotten_flesh", 12))))
    }

    @Test
    fun `an unrelated item in the grid blocks the craft`() {
        assertNull(take(grid(Stack("slime_ball", 3), Stack("bucket", 1), Stack("rotten_flesh", 3), Stack("dirt", 1))))
    }

    @Test
    fun `no bucket does not craft`() {
        assertNull(take(grid(Stack("slime_ball", 3), Stack("rotten_flesh", 3))))
    }

    @Test
    fun `the same item needed twice is counted together`() {
        // The slime bucket uses slime balls as both the extra item and the mob item.
        val slime = listOf(need("bucket", 1), need("slime_ball", 3), need("slime_ball", 3))
        assertContentEquals(intArrayOf(6, 1, 0, 0, 0, 0, 0, 0, 0), take(grid(Stack("slime_ball", 10), Stack("bucket", 1)), slime))
        assertNull(take(grid(Stack("slime_ball", 5), Stack("bucket", 1)), slime))
    }

    @Test
    fun `dangerous recipe with three extra kinds works from stacks`() {
        val blaze = listOf(need("bucket", 1), need("slime_block", 3), need("iron_chain", 2), need("blaze_rod", 3))
        val slots = grid(Stack("iron_chain", 64), Stack("slime_block", 4), Stack("bucket", 3), Stack("blaze_rod", 3))
        assertContentEquals(intArrayOf(2, 3, 1, 3, 0, 0, 0, 0, 0), take(slots, blaze))
    }
}
