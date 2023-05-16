package com.ifood.eos.utils

object CloudWatchMetricsUtils {
  // Função para compor a mensagem de erro de streams relacionada ao Cloudwatch a ser enviada no Slack
  def buildCloudwatchSlackStreamMessage(jobName: String, product: String, queryId: String, runId: String, notebookUrl: String, exception: Throwable): String = {
    s"""
<!channel>
:ahhhhhhhhh: *Cloudwatch Failure Alert* :ahhhhhhhhh:

Error while sending data to cloudwatch:

*Job Name*: $jobName
*Product*: $product
*Query ID*: $queryId
*Run ID*: $runId
*Notebook URL*: `$notebookUrl`

*Exception*:
${exception.getMessage.substring(0, 1000) + "..."}
    """.stripMargin
  }

  // Função para compor a mensagem de erro de jobs relacionada ao Cloudwatch a ser enviada no Slack
  def buildCloudwatchSlackJobMessage(jobName: String, owner: String, stageId: String, taskId: String, notebookUrl: String, exception: Throwable): String = {
    s"""
<!channel>
:ahhhhhhhhh: *Cloudwatch Failure Alert* :ahhhhhhhhh:

Error while sending data to cloudwatch:

*Job Name*: $jobName
*Owner*: $owner
*Stage ID*: $stageId
*Task ID*: $taskId
*Notebook URL*: `$notebookUrl`

*Exception*:
${exception.getMessage.substring(0, 1000) + "..."}
    """.stripMargin
  }
}
