package br.com.ttj

import java.util.UUID
import scala.util.Random

package object utils {

  def generateNullableUUID: String = {
    val random: Random = new Random()

    if (random.nextInt(100) % 7 == 0) null
    else UUID.randomUUID().toString
  }

  def generateNullableTimestamp: String = {
    val random: Random = new Random()

    if (random.nextInt(100) % 13 == 0) null
    else {
      val year: Int = 2023
      val month: Int = random.nextInt(12) + 1
      val day: Int = random.nextInt(20) + 1
      val hour: Int = random.nextInt(24)
      val minutes: Int = random.nextInt(60)
      val seconds: Int = random.nextInt(60)

      f"$year-$month%02d-$day%02d $hour%02d:$minutes%02d:$seconds%02d"
    }
  }

  def generateNullableDouble: Option[Double] = {
    val random: Random = new Random()

    if (random.nextInt(100) % 8 == 0) None
    else if (random.nextInt(100) % 15 == 0) Some(random.nextDouble() * random.nextInt(1000) * -1.0)
    else Some(random.nextDouble() * random.nextInt(1000))
  }
}
