package integrations.pipelines

import dsl.QueryExecutionData

trait RunnablePipeline {
  val data: QueryExecutionData

  def run(): Unit
}
