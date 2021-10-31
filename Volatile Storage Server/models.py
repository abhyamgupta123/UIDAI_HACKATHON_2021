from typing import Optional
from pydantic import BaseModel
from constants import SERVER_LINK


class DataBlock(BaseModel):
    data: str


class CreateResponse(BaseModel):
    link: str = SERVER_LINK+"/file/<UID>"


class GetFileResponse(BaseModel):
    status: str = "OK"
    file: Optional[DataBlock]
    remaining_time: Optional[int]
