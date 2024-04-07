package year2015.day15

import utils.Ops

import scala.util.matching.Regex

object Solution extends App with Ops:
  private val input  = loadMultilineInput(2015, 15)
  private val sample = loadMultilineInput(2015, 15, "sample.txt")

  private val score: Ingredient => Int =
    i => (i.capacity max 0) * (i.durability max 0) * (i.texture max 0) * (i.flavor max 0)

  private val ingredients = input.map(Ingredient.make)

  case class Ingredient(name: String, capacity: Int, durability: Int, flavor: Int, texture: Int, calories: Int, amount: Int = 0):
    def setAmount(n: Int): Ingredient =
      copy(amount = n)

    def +(that: Ingredient): Ingredient =
      val mixedCapacity   = capacity * amount + that.capacity * that.amount
      val mixedDurability = durability * amount + that.durability * that.amount
      val mixedFlavor     = flavor * amount + that.flavor * that.amount
      val mixedTexture    = texture * amount + that.texture * that.amount
      val mixedCalories   = calories * amount + that.calories * that.amount

      Ingredient("Mixture", mixedCapacity, mixedDurability, mixedFlavor, mixedTexture, mixedCalories, 1)

  object Ingredient:
    val pattern: Regex =
      "(\\w+): capacity (-?\\d+), durability (-?\\d+), flavor (-?\\d+), texture (-?\\d+), calories (-?\\d+)".r

    def make(s: String): Ingredient = s match
      case pattern(name, cap, dur, fla, text, cal) =>
        Ingredient(name, cap.toInt, dur.toInt, fla.toInt, text.toInt, cal.toInt)

      case _ => sys.error(s"invalid pattern @ $s")
  end Ingredient

  object Cookie:
    def getHighestScore(ingredients: List[Ingredient])(score: Ingredient => Int)(predicate: Ingredient => Boolean): Int =
      val totalIngredients = ingredients.length

      val totalCombinations = List
        .range(1, 101)
        .combinations(totalIngredients)
        .filter(_.sum == 100)
        .flatMap(_.permutations.toList)

      totalCombinations
        .map(ingredients.zip(_).map((ingredient, amount) => ingredient.setAmount(amount)))
        .map(_.reduce(_ + _))
        .filter(predicate)
        .map(score)
        .max
  end Cookie

  def part1(input: List[String]): Int =
    Cookie.getHighestScore(ingredients)(score)(_ => true)

  def part2(input: List[String]): Int =
    Cookie.getHighestScore(ingredients)(score)(_.calories == 500)

  def solve(input: List[String]): Unit =
    println(s"* Part 1: ${part1(input)}")
    println(s"* Part 2: ${part2(input)}")

  solve(input)
end Solution
