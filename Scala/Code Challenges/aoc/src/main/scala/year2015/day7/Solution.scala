package year2015.day7

import utils.Ops

object Solution extends App with Ops:
  private val input = loadMultilineInput(2015, 7)

  opaque type Circuit = Map[String, Int]

  object Circuit:
    def make: Circuit = Map()

    extension (self: Circuit)
      def get(wire: String): Int =
        self(wire)

      def contains(wires: String*): Boolean =
        wires.forall(self contains _)

      def set(wire: String, value: Int): Circuit =
        self.updated(wire, value)

      def set(w1: String, w2: String): Circuit =
        self.updated(w2, self(w1))

      def and(w1: String, w2: String, w3: String): Circuit =
        val v1  = self(w1); val v2 = self(w2)
        val res = v1 & v2
        self.updated(w3, res)

      def and(value: Int, w1: String, w2: String): Circuit =
        val res = value & self(w1)
        self.updated(w2, res)

      def or(w1: String, w2: String, w3: String): Circuit =
        val v1  = self(w1); val v2 = self(w2)
        val res = v1 | v2
        self.updated(w3, res)

      def or(value: Int, w1: String, w2: String): Circuit =
        val res = value | self(w1)
        self.updated(w2, res)

      def lshift(w1: String, n: Int, w2: String): Circuit =
        val v1 = self(w1); val res = v1 << n
        self.updated(w2, res)

      def rshift(w1: String, n: Int, w2: String): Circuit =
        val v1 = self(w1); val res = v1 >>> n
        self.updated(w2, res)

      def not(w1: String, w2: String): Circuit =
        val res = ~self(w1)
        self.updated(w2, res)

      def not(value: Int, w: String): Circuit =
        val res = ~value
        self.updated(w, res)
    end extension
  end Circuit

  def part1: Circuit =
    CircuitInstructions.applyAll(input)(Circuit.make)

  def part2: Circuit =
    val input2 = loadMultilineInput(2015, 7, "input2.txt")
    CircuitInstructions.applyAll(input2)(Circuit.make)

  def solve(): Unit =
    println(s"* Part 1: ${part1("a")}")
    println(s"* Part 2: ${part2("a")}")

  solve()
end Solution
