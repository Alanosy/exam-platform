FROM python:3.12-slim

WORKDIR /app

# 依赖（不再需要 build-essential：未引入 sentence-transformers / torch）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 应用代码（prompts/ 目录必须一起打进去，提示词是运行时资源）
COPY . .

EXPOSE 9221

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:9221/health').status==200 else 1)"

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "9221"]
