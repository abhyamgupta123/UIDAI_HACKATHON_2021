import uuid
import aioredis
from fastapi import FastAPI, Request
from models import DataBlock, CreateResponse, GetFileResponse
from constants import SERVER_LINK, REDIS_HOST, REDIS_PORT, REDIS_DB

redis: aioredis.Redis = aioredis.from_url(
    f"redis://{REDIS_HOST}:{REDIS_PORT}/{REDIS_DB}"
)

app = FastAPI()


@app.get("/")
async def read_root(request: Request):
    total_creations = await redis.get("total_creations") or 0
    return {"Hello": "World", "total_creations": total_creations}


@app.post("/create", response_model=CreateResponse)
async def create_data(data_obj: DataBlock):
    file_id = ''.join(str(uuid.uuid4()).split("-"))

    await redis.set(file_id, data_obj.data, 3600)
    await redis.incr("total_creations")

    resp = CreateResponse(link=SERVER_LINK+"/file/"+file_id)
    return resp


@app.get('/file/{file_id}', response_model=GetFileResponse)
async def get_file(file_id: str):
    file_cont = await redis.get(file_id)
    rem = await redis.ttl(file_id)
    resp = GetFileResponse(status="NOT_OK")
    if file_cont:
        resp.status = "OK"
        resp.file = DataBlock(data=file_cont.decode())
        resp.remaining_time = rem

    return resp


@app.delete('/file/{file_id}')
async def delete_file(file_id: str):
    await redis.delete(file_id)
    return {"status": "OK"}
