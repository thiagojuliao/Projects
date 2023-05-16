package com.ifood.eos.core

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, StandardUnit}
import com.ifood.eos.utils.SparkListenerUtils.CloudWatchNamespace
import slack.api.SlackApiClient

import scala.collection.mutable

trait CloudWatchMetrics {

  private def pushMetric(dimensionItems: mutable.Map[String, String], metricName: String, value: Double, unit: StandardUnit)(implicit cw: AmazonCloudWatch, namespace: CloudWatchNamespace): Unit = {
    val dimensions = new java.util.ArrayList[Dimension]()

    for ((k, v) <- dimensionItems) {
      val dimension = new Dimension().withName(k).withValue(v)
      dimensions.add(dimension)
    }

    val datum = new MetricDatum()
      .withMetricName(metricName)
      .withUnit(unit)
      .withValue(value)
      .withDimensions(dimensions)

    val request = new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(datum)

    val response = cw.putMetricData(request)

    if (response.getSdkHttpMetadata.getHttpStatusCode != 200) {
      println("Failed pushing CloudWatch Metric with RequestId: " + response.getSdkResponseMetadata.getRequestId)
      println("Response Status code: " + response.getSdkHttpMetadata.getHttpStatusCode)
    }
  }

  private[core] def pushCountMetric(dimensionItems: mutable.Map[String, String], metricName: String, value: Double)(implicit cw: AmazonCloudWatch, namespace: CloudWatchNamespace) : Unit = {
    this.pushMetric(dimensionItems, metricName, value.doubleValue(), StandardUnit.Count)
  }

  def pushToCloudWatch(client: SlackApiClient, metadata: mutable.HashMap[String, Any]): Unit
}
