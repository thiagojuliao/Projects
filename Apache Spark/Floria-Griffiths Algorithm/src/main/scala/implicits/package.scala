import org.apache.spark.sql.{DataFrame, Row}

package object implicits {

  implicit class LazyDisplay(dataframe: DataFrame) {
    def d(): Unit = dataframe.show()
  }

  implicit class CollectPrinter(arr: Array[Row]) {
    def vp: String = arr.flatMap(_.toSeq).mkString("[", ", ", "]")

    def up: String = arr.map(_.getDouble(1)).mkString("[", ", ", "]")

    def tp: String = arr.map(_.getDouble(1)).mkString("[", ", ", "]")
  }

  implicit val rowOrdering: Ordering[Row] = (r1: Row, r2: Row) => r1.getInt(0).compare(r2.getInt(0))
}
