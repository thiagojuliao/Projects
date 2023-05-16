import re
import json
import IPython

from pyspark.sql.types import *
from pyspark.sql import SparkSession
from pyspark.rdd import _load_from_socket
from pyspark.serializers import BatchedSerializer, PickleSerializer


class SparkStreamListener(object):
  query_data_schema = StructType(
    [
      StructField("query_id", StringType()),
      StructField("run_id", StringType()),
      StructField("timestamp", StringType()),
      StructField("query_name", StringType()),
      StructField("batch_id", LongType()),
      StructField("batch_duration", LongType()),
      StructField("add_batch_duration", LongType()),
      StructField("get_batch_duration", LongType()),
      StructField("latest_offset_duration", LongType()),
      StructField("query_planning_duration", LongType()),
      StructField("trigger_execution_duration", LongType()),
      StructField("wal_commit_duration", LongType()),
      StructField("input_rows", LongType()),
      StructField("output_rows", LongType()),
      StructField("input_rate", LongType()),
      StructField("process_rate", LongType()),
      StructField("job_name", StringType()),
      StructField("product", StringType()),
      StructField("zone", StringType()),
      StructField("namespace", StringType()),
      StructField("dataset", StringType())
    ]
  )
  
  metrics_schema = StructType(
    [
      StructField("job_name", StringType()),
      StructField("query_name", StringType()),
      StructField("total_batches", LongType()),
      StructField("total_batch_duration", LongType()),
      StructField("min_batch_duration", LongType()),
      StructField("batch_duration_quantiles", ArrayType(LongType())),
      StructField("max_batch_duration", LongType()),
      StructField("total_add_batch_duration", LongType()),
      StructField("min_add_batch_duration", LongType()),
      StructField("add_batch_duration_quantiles", ArrayType(LongType())),
      StructField("max_add_batch_duration", LongType()),
      StructField("total_get_batch_duration", LongType()),
      StructField("min_get_batch_duration", LongType()),
      StructField("get_batch_duration_quantiles", ArrayType(LongType())),
      StructField("max_get_batch_duration", LongType()),
      StructField("total_latest_offset_duration", LongType()),
      StructField("min_latest_offset_duration", LongType()),
      StructField("latest_offset_duration_quantiles", ArrayType(LongType())),
      StructField("max_latest_offset_duration", LongType()),
      StructField("total_query_planning_duration", LongType()),
      StructField("min_query_planning_duration", LongType()),
      StructField("query_planning_duration_quantiles", ArrayType(LongType())),
      StructField("max_query_planning_duration", LongType()),
      StructField("total_trigger_execution_duration", LongType()),
      StructField("min_trigger_execution_duration", LongType()),
      StructField("trigger_execution_duration_quantiles", ArrayType(LongType())),
      StructField("max_trigger_execution_duration", LongType()),
      StructField("total_wal_commit_duration", LongType()),
      StructField("min_wal_commit_duration", LongType()),
      StructField("wal_commit_duration_quantiles", ArrayType(LongType())),
      StructField("max_wal_commit_duration", LongType()),
      StructField("total_input_rows", LongType()),
      StructField("min_input_rows", LongType()),
      StructField("input_rows_quantiles", ArrayType(LongType())),
      StructField("max_input_rows", LongType()),
      StructField("total_input_rate", LongType()),
      StructField("min_input_rate", LongType()),
      StructField("input_rate_quantiles", ArrayType(LongType())),
      StructField("max_input_rate", LongType()),
      
      StructField("total_output_rows", LongType()),
      StructField("min_output_rows", LongType()),
      StructField("output_rows_quantiles", ArrayType(LongType())),
      StructField("max_output_rows", LongType()),
      
      StructField("total_process_rate", LongType()),
      StructField("min_process_rate", LongType()),
      StructField("process_rate_quantiles", ArrayType(LongType())),
      StructField("max_process_rate", LongType())
    ]
  )
  
  def __init__(self, product, slack_channel_id, cloud_watch_namespace):
    self.spark = SparkSession.builder.getOrCreate()
    self.sc = self.spark.sparkContext
    self.dbutils = IPython.get_ipython().ns_table["user_global"]["dbutils"].shell
    self.owner, self.job_name = self.__get_session_info__()
    self.notebook_url = self.__get_notebook_url__()
    self.jvmSSL = self.sc._jvm.com.ifood.eos.core.SparkStreamListener
    self.ssl = self.sc._jvm.com.ifood.eos.core.SparkStreamListener.apply(
      self.job_name, 
      product, 
      slack_channel_id, 
      cloud_watch_namespace,
      self.notebook_url
    )
    
    print(self.jvmSSL.getInfo())
    
  @property
  def listener(self):
    return self.ssl
  
  def __get_session_info__(self):
    prod_path_regex = r"/prd/etl/(\w+)/(\w+)/(\w+)"
    dev_path_regex = r"/Users/(\w+\.\w+@ifood.com.br)?.+/(.+)"

    notebook_path = self.dbutils.entry_point.getDbutils().notebook().getContext().notebookPath().get()

    match_prod = re.search(prod_path_regex, notebook_path)
    match_dev = re.search(dev_path_regex, notebook_path)

    if match_prod:
      return ("iFood Prod", "{}_{}.{}".format(match_prod.group(2), match_prod.group(1), match_prod.group(3)))
    return (match_dev.group(1), match_dev.group(2))
  
  def __get_notebook_url__(self):
    infos = json.loads(self.dbutils.entry_point.getDbutils().notebook().getContext().toJson())
    
    try:
      browser_hash = "/".join(infos["tags"]["browserHash"].split("/")[:2])
      api_url = infos["extraContext"]["api_url"]

      return api_url + "/" + browser_hash
    except KeyError:
      run_id = infos["tags"]["idInJob"].replace("\\", "")
      job_url = "https://ifood-prod.cloud.databricks.com/#job/" + infos["tags"]["jobId"] + "/run/"
                                                
      return job_url + "/" + run_id                               
  
  def addQueryNames(self, *names):
    self.ssl.addQueryNames(list(names))
  
  def getQueryData(self):
    sock_info = self.ssl.getQueryData().collectToPython()
    rows = list(_load_from_socket(sock_info, BatchedSerializer(PickleSerializer())))
    
    return self.spark.createDataFrame(rows, SparkStreamListener.query_data_schema)
    
  def getStreamMetrics(self):
    sock_info = self.ssl.getStreamMetrics().collectToPython()
    rows = list(_load_from_socket(sock_info, BatchedSerializer(PickleSerializer())))
    
    return self.spark.createDataFrame(rows, SparkStreamListener.metrics_schema)
  
  def clear(self):
    self.ssl.clear()