/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package core

import core.implicits._
import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.sql.execution.adaptive.{AdaptiveSparkPlanExec, BroadcastQueryStageExec, QueryStageExec, ShuffleQueryStageExec}
import org.apache.spark.sql.execution.columnar.InMemoryTableScanExec
import org.apache.spark.sql.execution.exchange.ReusedExchangeExec
import org.apache.spark.sql.execution.metric.SQLMetricInfo
import org.apache.spark.sql.execution.{InputAdapter, ReusedSubqueryExec, SparkPlan}
import org.apache.spark.sql.internal.SQLConf

/**
 * :: DeveloperApi ::
 * Stores information about a SQL SparkPlan (Customized for this project).
 */
@DeveloperApi
class SparkPlanInfo(
                     val nodeName: String,
                     val simpleString: String,
                     val children: Seq[SparkPlanInfo],
                     val props: Map[String, Any],
                     val metrics: Seq[SQLMetricInfo]
                   ) {

  override def hashCode(): Int = {
    // hashCode of simpleString should be good enough to distinguish the plans from each other
    // within a plan
    simpleString.hashCode
  }

  override def equals(other: Any): Boolean = other match {
    case o: SparkPlanInfo =>
      nodeName == o.nodeName && simpleString == o.simpleString && children == o.children
    case _ => false
  }
}

object SparkPlanInfo {

  def fromSparkPlan(plan: SparkPlan): SparkPlanInfo = plan match {
    // Every node that we want to be discarded should be pattern matched here
    case InputAdapter(child) => fromSparkPlan(child)
    case s: ShuffleQueryStageExec => fromSparkPlan(s.plan)
    case b: BroadcastQueryStageExec => fromSparkPlan(b.plan)

    // All the other ones to be included
    case _ =>
      val children = plan match {
        case ReusedExchangeExec(_, child) => child :: Nil
        case ReusedSubqueryExec(child) => child :: Nil
        case a: AdaptiveSparkPlanExec => a.executedPlan :: Nil
        case stage: QueryStageExec => stage.plan :: Nil
        case inMemTab: InMemoryTableScanExec => inMemTab.relation.cachedPlan :: Nil
        case _ => plan.children ++ plan.subqueries
      }

      val metrics = plan.metrics.toSeq.map { case (key, metric) =>
        new SQLMetricInfo(metric.name.getOrElse(key), metric.id, metric.metricType)
      }

      val props = plan.props

      new SparkPlanInfo(
        plan.getClass.getSimpleName,
        plan.simpleString(SQLConf.get.maxToStringFields),
        children.map(fromSparkPlan),
        props,
        metrics
      )
  }
}
