import json
import boto3
import base64

# Função para realizar o decode da string de bytes enviada ao Kinesis
def decode_string(uncoded_string, string_format="utf-8"):
    return str(base64.b64decode(uncoded_string), string_format)


# Função que retorna os dados trazidos pelo Kinesis em um dicionário
# Os eventos gerados tem o seguinte formato:
#{
#   "Records": [    
#       {
#           "kinesis": {
#               "kinesisSchemaVersion": 1.0,
#               "partitionKey": 1,
#               "sequenceNumber": "49608543445093149138977617739892766301713303732232388610",
#               "data": "e2JvZHk9eyJ0b3RhbCI6MSwiZGF0YSI6W3siaWQiOiI4OTk1YzQwZi0xYz...",
#               "approximateArrivalTimestamp": 1593815343.855
#           
#           },
#           "eventSource": "aws:kinesis",
#           "eventVersion": 1.0,
#           "eventID": "shardId-000000000000:49608543445093149138977617739892766301713303732232388610",
#           "eventName": "aws:kinesis:record",
#           "invokeIdentityArn": "arn:aws:iam::563718358426:role/FirehoseFileAggregatorRole",
#           "awsRegion": "us-east-1",
#           "eventSourceARN": "arn:aws:kinesis:us-east-1:563718358426:stream/wavy-api-whatsapp-stream"
#       }
#    ]
#}
def get_kinesis_data(event):
    kinesis_data = []
    
    for record in event["Records"]:
        data = record["kinesis"]["data"]
        decoded_data = decode_string(data)
        kinesis_data.append(json.loads(decoded_data))
    
    return kinesis_data

   
# Função para inserção de dados em nosso buffer ou sua criação caso não exista
def feed_buffer(buffer, data, append=True):
    if append:
        buffer_file = json.loads(buffer.get()["Body"].read())
        buffer_file += data
        buffer.put(Body=json.dumps(buffer_file))
    else:
        buffer.put(Body=json.dumps(data))


# Função que envia o buffer a uma subpasta do S3
def deliver_buffer(buffer, destination_bucket, destination_key, file_prefix):
    s3 = boto3.resource("s3")
    bucket = s3.Bucket(destination_bucket)
    
    indexes = []
    
    for obj in bucket.objects.filter(Prefix=destination_key + file_prefix):
        index = int(obj.key.split("/")[2].split(".")[0].split(file_prefix)[1])
        indexes.append(index)
    
    if indexes:
        new_index = str(max(indexes) + 1).rjust(2, "0")
        file_name = file_prefix + new_index + ".json"
        
        obj = s3.Object(destination_bucket, destination_key + file_name)
        
        data = json.loads(buffer.get()["Body"].read())
        obj.put(Body=json.dumps(data))
    else:
        file_name = file_prefix + "00.json"
        obj = s3.Object(destination_bucket, destination_key + file_name)
        
        data = json.loads(buffer.get()["Body"].read())
        obj.put(Body=json.dumps(data))
