package com.ifood.eos.utils

import org.apache.spark.sql.Row

import java.util.Locale
import java.math.{MathContext, RoundingMode}

object SparkReporterUtils {
  val projectEOSImageSource = "https://i.pinimg.com/originals/f7/46/fc/f746fccce17d810554a5d40ce7a985f5.gif"
  val taskDurationImageSource = "https://d1nhio0ox7pgb.cloudfront.net/_img/g_collection_png/standard/512x512/hourglass.png"
  val shuffleMetricsImageSource = "https://d1nhio0ox7pgb.cloudfront.net/_img/o_collection_png/green_dark_grey/256x256/plain/arrow_shuffle.png"
  val memoryManagementImageSource = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFBmvDtJgrVSmi1k32wlDFOkZ6CTcAn_ZqPw&usqp=CAU"
  val tipsImageSource = "https://t3.ftcdn.net/jpg/02/98/32/02/360_F_298320299_nKqLylfxokJslTshibgbEc3pJTHbv52Z.jpg"
  val databricksLogoImageSource = "https://storage.googleapis.com/wp-noticias/2021/02/3b198fa1-databricks_logo.png"
  val apacheSparkLogoImageSource = "https://dv-website.s3.amazonaws.com/uploads/2015/06/spark-logo.png"
  val scalaLogoImageSource = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5Ge4Q3DN6I5OHRK89KxD72mjgCmiWx9VusydLhssQa47mKrzVFsh3USOVqhMge5svHg&usqp=CAU"
  val ifoodLogoImageSource = "https://emoji.slack-edge.com/T017UJXRZQ8/ifoood/40f1e0c1bb7a5c83.gif"
  val sparkJobImageSource = "data:image/png;base64,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"
  val taskFailureImageSource = "data:image/png;base64,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"

  // Formatador para os campos de valores temporais
  // https://github.com/apache/spark/blob/master/core/src/main/scala/org/apache/spark/ui/UIUtils.scala)
  def formatDuration(milliseconds: Long): String = {
    if (milliseconds < 100) {
      return "%d ms".format(milliseconds)
    }
    val seconds = milliseconds.toDouble / 1000
    if (seconds < 1) {
      return "%.1f s".format(seconds)
    }
    if (seconds < 60) {
      return "%.0f s".format(seconds)
    }
    val minutes = seconds / 60
    if (minutes < 10) {
      return "%.1f min".format(minutes)
    } else if (minutes < 60) {
      return "%.0f min".format(minutes)
    }
    val hours = minutes / 60
    "%.1f h".format(hours)
  }

  // Formatador para o tamanho de bytes
  // https://github.com/apache/spark/blob/master/core/src/main/scala/org/apache/spark/util/Utils.scala
  def bytesToString(size: Long): String = bytesToString(BigInt(size))

  def bytesToString(size: BigInt): String = {
    val EiB = 1L << 60
    val PiB = 1L << 50
    val TiB = 1L << 40
    val GiB = 1L << 30
    val MiB = 1L << 20
    val KiB = 1L << 10

    if (size >= BigInt(1L << 11) * EiB) {
      // The number is too large, show it in scientific notation
      BigDecimal(size, new MathContext(3, RoundingMode.HALF_UP)).toString() + " B"
    } else {
      val (value, unit) = {
        if (size >= 2 * EiB) {
          (BigDecimal(size) / EiB, "EiB")
        } else if (size >= 2 * PiB) {
          (BigDecimal(size) / PiB, "PiB")
        } else if (size >= 2 * TiB) {
          (BigDecimal(size) / TiB, "TiB")
        } else if (size >= 2 * GiB) {
          (BigDecimal(size) / GiB, "GiB")
        } else if (size >= 2 * MiB) {
          (BigDecimal(size) / MiB, "MiB")
        } else if (size >= 2 * KiB) {
          (BigDecimal(size) / KiB, "KiB")
        } else {
          (BigDecimal(size), "B")
        }
      }
      "%.1f %s".formatLocal(Locale.US, value, unit)
    }
  }

  // Função para impressão das colunas de uma tabela
  def printColumns(cols: Row): String = {
    cols.toSeq.map(c => s"""<td class="metrics-data">$c</td>""").reduce(_ + "\n" + _)
  }

  // Função para impressão das linhas de uma tabela
  def printRows(rows: Array[Row]): String = {
    val tableRows = for {
      row <- rows
    } yield {
      s"""
        <tr class="metrics-data">
          ${printColumns(row)}
        </tr>
      """
    }

    tableRows.reduce(_ + "\n" + _)
  }

  // Função para imprimir o título de uma tabela
  def printTableTitle(title: String, fontSize: Int, color: String, imageSource: String, imgSize: Int = 30): String = {
    s"""
      <table align="center">
        <tr>
          <td style="font-size:${fontSize}px; color:$color"><b>$title</b></td>
          <td><img src="$imageSource" width="${imgSize}px" heigth="${imgSize}px"></td>
        </tr>
      </table>
    """
  }

  // Adiciona o rodapé aos documentos HTML com a marca registrada de Core
  def addFooterSection(): String = {
    s"""
     <footer>
       <p align="center"><b>DArch Core Team</b> &copy All rights reserved.</p><br>
       <table align="center">
         <tr>
           <td><img src="$databricksLogoImageSource" with="40px" height="40px"></td>
           <td>        </td>
           <td><img src="$apacheSparkLogoImageSource" with="60px" height="60px"></td>
           <td>        </td>
           <td><img src="$scalaLogoImageSource" with="30px" height="30px"></td>
           <td>        </td>
           <td><img src="$ifoodLogoImageSource" with="40px" height="40px"></td>
         </tr>
       </table>
     </footer>
   """
  }
}
