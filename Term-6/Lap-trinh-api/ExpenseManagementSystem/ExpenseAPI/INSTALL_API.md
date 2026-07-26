# INSTALL_API.md — ExpenseAPI

## Yêu cầu hệ thống

Trước khi bắt đầu, đảm bảo máy đã cài sẵn:

- **Python 3.10 trở lên** — kiểm tra bằng `python --version`
- **SQL Server** (khuyến nghị SQL Server Express)
- **Microsoft ODBC Driver 17 hoặc 18 for SQL Server** — đây là driver cấp hệ điều hành, `pip` không cài được, phải tải thủ công
- **Git**

> ⚠️ **Lưu ý quan trọng:** ODBC Driver phải được cài trước khi chạy API, nếu không `pyodbc` sẽ báo lỗi dù đã `pip install` thành công.
>
> Tải tại: https://learn.microsoft.com/en-us/sql/connect/odbc/download-odbc-driver-for-sql-server

---

## Bước 1 — Clone repository

```bash
git clone <your-repo-url>
cd <your-api-folder>
```

---

## Bước 2 — Tạo môi trường ảo (bắt buộc)

> ⚠️ **Không bỏ qua bước này.** Nếu cài thẳng vào Python hệ thống, các máy khác nhau sẽ có version xung đột và báo thiếu thư viện dù đã cài rồi.

### Windows
```bash
python -m venv .venv
.venv\Scripts\activate
```

### macOS / Linux
```bash
python3 -m venv .venv
source .venv/bin/activate
```

Sau khi activate thành công, terminal sẽ hiện `(.venv)` ở đầu dòng. **Mọi lệnh phía sau đều phải chạy trong terminal này.**

---

## Bước 3 — Cài dependencies

```bash
# Upgrade pip trước để tránh lỗi resolve
python -m pip install --upgrade pip

# Cài toàn bộ thư viện
pip install -r requirements.txt
```

> ⚠️ **Lưu ý:** Luôn dùng `python -m pip` thay vì chỉ `pip`, để đảm bảo cài đúng vào môi trường ảo đang active.

### Kiểm tra đã cài đủ chưa

```bash
pip list
```

Các package sau phải có mặt trong danh sách:

| Package | Vai trò |
|---|---|
| `fastapi` | Web framework chính |
| `uvicorn` | ASGI server để chạy API |
| `sqlalchemy` | ORM kết nối database |
| `pyodbc` | Driver kết nối SQL Server |
| `python-dotenv` | Đọc file `.env` |
| `python-jose` | Mã hóa / giải mã JWT token |
| `passlib` | Hash mật khẩu |
| `pydantic` | Validate dữ liệu request/response |
| `email-validator` | Validate định dạng email (dùng bởi pydantic) |
| `cryptography` | Phụ thuộc của `python-jose` (tự cài kèm) |

---

## Bước 4 — Tạo file `.env`

Tạo file `.env` ở thư mục gốc của project. File này **không được commit lên Git**.

### Dùng Windows Authentication (khuyến nghị nếu chạy local)

```env
DB_SERVER=.\SQLEXPRESS
DB_NAME=ExpenseDB
DB_TRUSTED_CONNECTION=yes

SECRET_KEY=change_this_to_a_random_secret_key
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=60
```

### Dùng SQL Server Authentication

```env
DB_SERVER=.\SQLEXPRESS
DB_NAME=ExpenseDB
DB_USERNAME=sa
DB_PASSWORD=your_password
DB_TRUSTED_CONNECTION=no

SECRET_KEY=change_this_to_a_random_secret_key
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=60
```

> ⚠️ **Không để nguyên `SECRET_KEY=change_this_...`** khi deploy thực tế. Dùng một chuỗi ngẫu nhiên dài ít nhất 32 ký tự.

---

## Bước 5 — Kiểm tra cấu hình `config.py`

File `app/core/config.py` phải đọc được cả hai kiểu authentication:

```python
import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    DB_SERVER: str = os.getenv("DB_SERVER", ".\\SQLEXPRESS")
    DB_NAME: str = os.getenv("DB_NAME", "ExpenseDB")
    DB_USERNAME: str = os.getenv("DB_USERNAME", "")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "")
    DB_TRUSTED_CONNECTION: str = os.getenv("DB_TRUSTED_CONNECTION", "yes")

    SECRET_KEY: str = os.getenv("SECRET_KEY", "change_this_secret")
    ALGORITHM: str = os.getenv("ALGORITHM", "HS256")
    ACCESS_TOKEN_EXPIRE_MINUTES: int = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 60))


settings = Settings()
```

---

## Bước 6 — Kiểm tra cấu hình `database.py`

File `app/core/database.py` phải tự chọn connection string dựa theo biến `DB_TRUSTED_CONNECTION`:

```python
from urllib.parse import quote_plus
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

from app.core.config import settings

if settings.DB_TRUSTED_CONNECTION.lower() == "yes":
    connection_string = (
        f"DRIVER={{SQL Server}};"
        f"SERVER={settings.DB_SERVER};"
        f"DATABASE={settings.DB_NAME};"
        f"Trusted_Connection=yes;"
    )
else:
    connection_string = (
        f"DRIVER={{SQL Server}};"
        f"SERVER={settings.DB_SERVER};"
        f"DATABASE={settings.DB_NAME};"
        f"UID={settings.DB_USERNAME};"
        f"PWD={settings.DB_PASSWORD};"
    )

DATABASE_URL = f"mssql+pyodbc:///?odbc_connect={quote_plus(connection_string)}"

engine = create_engine(DATABASE_URL, echo=False, future=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
```

---

## Bước 7 — Chạy API

```bash
python -m uvicorn app.main:app --reload
```

> ⚠️ **Dùng `python -m uvicorn`** (có `-m`), không phải chỉ `uvicorn`. Cách này đảm bảo dùng đúng uvicorn của môi trường ảo, tránh lỗi "command not found" hoặc chạy nhầm Python hệ thống.

API sẽ chạy tại:

```
http://127.0.0.1:8000
```

Mở trình duyệt hoặc Postman, truy cập địa chỉ trên. Nếu thành công sẽ thấy:

```json
{"message": "ExpenseAPI is running"}
```

Xem tài liệu API tự động tại: `http://127.0.0.1:8000/docs`

---

## Kiểm tra kết nối database

Tạo file `test_db.py` tạm ở thư mục gốc để kiểm tra:

```python
import pyodbc

conn_str = (
    "DRIVER={SQL Server};"
    "SERVER=.\\SQLEXPRESS;"
    "DATABASE=ExpenseDB;"
    "Trusted_Connection=yes;"
)

try:
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()
    cursor.execute("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES")
    tables = cursor.fetchall()
    print(f"Kết nối thành công! Tìm thấy {len(tables)} bảng:")
    for row in tables:
        print(f"  - {row[0]}")
except Exception as e:
    print(f"Lỗi kết nối: {e}")
```

Chạy: `python test_db.py`

Nếu in ra danh sách bảng → kết nối database ổn.

---

## Xử lý lỗi thường gặp

### Lỗi: "No module named X" dù đã cài

Nguyên nhân: Đang chạy Python của hệ thống, không phải của `.venv`.

```bash
# Kiểm tra Python nào đang được dùng
where python    # Windows
which python    # macOS/Linux
```

Đường dẫn phải chứa `.venv`. Nếu không, hãy activate lại:

```bash
.venv\Scripts\activate      # Windows
source .venv/bin/activate   # macOS/Linux
```

---

### Lỗi: `[Microsoft][ODBC Driver Manager] Data source name not found`

Nguyên nhân: Chưa cài ODBC Driver for SQL Server ở cấp hệ điều hành.

Tải và cài tại: https://learn.microsoft.com/en-us/sql/connect/odbc/download-odbc-driver-for-sql-server

---

### Lỗi liên quan `bcrypt` và `passlib`

Project này dùng `pbkdf2_sha256` (không phải `bcrypt`) nên không cần cài riêng `bcrypt`. Nếu gặp lỗi, kiểm tra `security.py`:

```python
pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")
```

---

### Lỗi: `localhost` không kết nối được SQL Server

Kiểm tra lần lượt:

1. Tên SQL Server instance có đúng là `SQLEXPRESS` không (vào SQL Server Configuration Manager để xem)
2. Database `ExpenseDB` đã được tạo chưa
3. SQL Server service đang chạy chưa (vào Services → SQL Server)
4. Windows Firewall có chặn port 1433 không

---

## Lệnh khởi động nhanh (tóm tắt)

```bash
# Chỉ cần chạy lần đầu
python -m venv .venv
pip install -r requirements.txt

# Mỗi lần muốn chạy API
.venv\Scripts\activate                          # Windows
# hoặc: source .venv/bin/activate              # macOS/Linux

python -m uvicorn app.main:app --reload
```
