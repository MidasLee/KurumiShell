from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from apirouter.xuanyuan_router import xuanyuan_router
import logging


logging.basicConfig(
    level=logging.INFO,  # 设置日志级别为 INFO
    format="%(levelname)s - %(asctime)s - %(message)s",  # 设置日志格式
    handlers=[logging.StreamHandler()]  # 输出到控制台
)
logger = logging.getLogger(__name__)

app = FastAPI()

# 允许跨域请求
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(xuanyuan_router, prefix="/api/xuanyuan")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app="main:app", host="0.0.0.0", port=8188, reload=True)
