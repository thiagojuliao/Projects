from pyspark.sql.types import *
from pyspark.sql.functions import *

data = list(map(lambda n: [n], range(1, 1000000)))
total_presents_01 = filter(sequence(lit(1), col("house_id")), lambda n: col("house_id") % n == 0)
total_presents_02 = filter(sequence(lit(1), col("house_id")), lambda n: (col("house_id") % n == 0) & (col("house_id") / n < 50))

df_01 = spark.createDataFrame(data, ["house_id"]) \
  .withColumn("total_presents", total_presents) \
  .withColumn("total_presents", aggregate("total_presents", lit(0).cast("long"), lambda acc, n: acc + n))

df_02 = spark.createDataFrame(data, ["house_id"]) \
  .withColumn("total_presents", total_presents_02) \
  .withColumn("total_presents", lit(11).cast("long") * aggregate("total_presents", lit(0).cast("long"), lambda acc, n: acc + n))