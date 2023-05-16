import json
import boto3

from datetime import *
from functions import *

def lambda_handler(event, context):
    dt_ref = str(datetime.today())[0:10]
    dt_old = str(datetime.strptime(dt_ref, "%Y-%m-%d") - timedelta(days=1))[0:10]
    dt_ref_tag = dt_ref.replace("-", "")
    dt_old_tag = dt_old.replace("-", "")
    
    s3 = boto3.resource("s3")
    buffer = s3.Object(
        "BUCKET_NAME", 
        "SUB_FOLDER/buffer_{}.json".format(dt_ref_tag)
        )
    old_buffer = s3.Object(
        "BUCKET_NAME",
        "SUB_FOLDER/buffer_{}.json".format(dt_old_tag)
        )
    max_buffer_size = 0.005 * 1024 * 1024 # Valor em MB
    
    destination_bucket = "BUCKET_NAME"
    destination_key = "SUB_FOLDER/{}/".format(dt_ref)
    old_destination_key = "SUB_FOLDER/{}/".format(dt_old)
    
    # Checamos se existe algum buffer pendente do dia anterior
    # Se sim transferimos pra seu respectivo folder mesmo não atingindo o tamanho máximo
    print("Checando a existência do buffer do dia {}...".format(dt_old))
    
    try:
        deliver_buffer(old_buffer, destination_bucket, old_destination_key, "data")
    except:
        print("Nenhum arquivo antigo encontrado!")
        
    # Extrai os dados do batch oriundo do Kinesis
    data = get_kinesis_data(event)
    
    # Verifica a existência do arquivo buffer_YYYYMMDD.json
    # Caso exista iremos appendar o novo conteúdo e sobrescrever caso o tamanho
    # seja menor que o limite especificado do buffer
    # Caso contrário, criaremos o arquivo buffer_YYYYMMDD.json com o conteúdo do payload
    try:
        buffer_size = buffer.get()["ContentLength"]
        
        if buffer_size >= max_buffer_size:
            deliver_buffer(buffer, destination_bucket, destination_key, "data")
            feed_buffer(buffer, data, False)
            return "Arquivo buffer_{}.json movido com sucesso!".format(dt_ref_tag)
        else:
            feed_buffer(buffer, data)
        return "Arquivo buffer_{}.json atualizado com sucesso!".format(dt_ref_tag)
    except:
        feed_buffer(buffer, data, False)
        return "Arquivo buffer_{}.json criado com sucesso!".format(dt_ref_tag)
