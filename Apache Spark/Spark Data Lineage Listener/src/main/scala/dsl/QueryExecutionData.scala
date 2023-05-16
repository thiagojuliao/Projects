package dsl

import java.text.SimpleDateFormat

object QueryExecutionData {

  final val SUCCEEDED = "succeeded"
  final val FAILED = "failed"

  private final val tsf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def apply(
             queryId: String, executionId: Long, trigger: String, duration: Long,
             status: String, failureReason: Option[String], jobs: List[Int],
             stages: List[Int], data: List[SparkLineageData]
           ): QueryExecutionData = {
    new QueryExecutionData(
      queryId,
      executionId,
      tsf.format(System.currentTimeMillis()),
      trigger,
      duration,
      status,
      failureReason,
      jobs,
      stages,
      data
    )
  }
}

case class QueryExecutionData(
                               queryId: String,
                               executionId: Long,
                               timestamp: String,
                               trigger: String,
                               durationNs: Long,
                               status: String,
                               failureReason: Option[String],
                               jobIds: List[Int],
                               stageIds: List[Int],
                               data: List[SparkLineageData]
                             )
