# app.py – API GET tìm giảng viên theo giới tính và tên bộ môn
from flask import Flask, request, jsonify
import pyodbc
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# -------------------------------------------------------
# SQL tạo bảng + dữ liệu mẫu (chạy 1 lần trong SQL Server)
# -------------------------------------------------------
# CREATE TABLE tblBoMon (
#     MaBM   VARCHAR(10)   PRIMARY KEY,
#     TenBM  NVARCHAR(100)
# );
# CREATE TABLE tblGV (
#     MaGV     VARCHAR(10)  PRIMARY KEY,
#     TenGV    NVARCHAR(100),
#     BoMon    VARCHAR(10)  REFERENCES tblBoMon(MaBM),
#     GioiTinh NVARCHAR(5),
#     DiaChi   NVARCHAR(200),
#     SoDT     VARCHAR(15)
# );
# INSERT INTO tblBoMon VALUES
#     ('BM01', N'Công nghệ thông tin'),
#     ('BM02', N'Toán - Tin'),
#     ('BM03', N'Vật lý');
# INSERT INTO tblGV VALUES
#     ('GV001', N'Nguyễn Văn An',  'BM01', N'Nam', N'Hà Nội',  '0901000001'),
#     ('GV002', N'Trần Thị Bình',  'BM01', N'Nữ',  N'Hà Nội',  '0901000002'),
#     ('GV003', N'Lê Văn Cường',   'BM02', N'Nam', N'Hà Nam',   '0901000003'),
#     ('GV004', N'Phạm Thị Dung',  'BM02', N'Nữ',  N'Hưng Yên','0901000004'),
#     ('GV005', N'Hoàng Văn Em',   'BM03', N'Nam', N'Hà Nội',  '0901000005');
# -------------------------------------------------------

def get_conn():
    return pyodbc.connect(
        "DRIVER={SQL Server};"
        "SERVER=HOANGNGUYEN\\SQLEXPRESS;"
        "DATABASE=QLGiangVienDB;"
        "Trusted_Connection=yes;"
    )

@app.route('/giangvien', methods=['GET'])
def get_gv():
    try:
        gioitinh = request.args.get('gioitinh', '').strip()
        tenbomon  = request.args.get('tenbomon',  '').strip()

        conn   = get_conn()
        cursor = conn.cursor()

        # WHERE 1=1 để dễ nối điều kiện động
        query  = """
            SELECT gv.MaGV, gv.TenGV, gv.GioiTinh, gv.DiaChi, gv.SoDT, bm.TenBM
            FROM   tblGV gv
            JOIN   tblBoMon bm ON gv.BoMon = bm.MaBM
            WHERE  1=1
        """
        params = []

        if gioitinh:
            query += " AND gv.GioiTinh = ?"          # tìm chính xác giới tính
            params.append(gioitinh)

        if tenbomon:
            query += " AND bm.TenBM LIKE ?"          # tìm gần đúng tên bộ môn
            params.append('%' + tenbomon + '%')

        cursor.execute(query, params)

        data = []
        for row in cursor.fetchall():
            data.append({
                "MaGV":    row.MaGV,
                "TenGV":   row.TenGV,
                "GioiTinh":row.GioiTinh,
                "DiaChi":  row.DiaChi,
                "SoDT":    row.SoDT,
                "TenBM":   row.TenBM
            })

        conn.close()
        return jsonify(data)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)