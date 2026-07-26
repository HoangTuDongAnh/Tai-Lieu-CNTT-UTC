import flask
import pyodbc
from flask_cors import CORS

app = flask.Flask(__name__)
CORS(app)

conn_str = (
    "DRIVER={SQL Server};"
    "SERVER=HOANGNGUYEN\\SQLEXPRESS;"
    "DATABASE=DuLieu;"
    "Trusted_Connection=yes;"
)

conn = pyodbc.connect(conn_str)

@app.route('/', methods=['GET'])
def home():
    return flask.jsonify({
        "message": "API san pham dang chay",
        "routes": [
            "/sanpham/getall",
            "/sanpham/search?tenSP=...&tenCL=...",
            "/sanpham/tonkho",
            "/sanpham/add",
            "/sanpham/update/<id>",
            "/sanpham/delete/<id>"
        ]
    }), 200


@app.route('/sanpham/getall', methods=['GET'])
def getAllSanPham():
    try:
        cursor = conn.cursor()
        sql = """
            SELECT sp.MaSP, sp.TenSP, sp.MaCL, cl.TenCL, sp.MoTa, sp.GiaNhap, sp.GiaBan, sp.SoLuong
            FROM tblSanPham sp
            INNER JOIN tblChatLieu cl ON sp.MaCL = cl.MaCL
        """
        cursor.execute(sql)

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            row = dict(zip(keys, val))
            results.append(row)

        return flask.jsonify(results), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/sanpham/search', methods=['GET'])
def searchSanPham():
    try:
        ten_sp = flask.request.args.get("tenSP", "")
        ten_cl = flask.request.args.get("tenCL", "")

        cursor = conn.cursor()
        sql = """
            SELECT sp.MaSP, sp.TenSP, sp.MaCL, cl.TenCL, sp.MoTa, sp.GiaNhap, sp.GiaBan, sp.SoLuong
            FROM tblSanPham sp
            INNER JOIN tblChatLieu cl ON sp.MaCL = cl.MaCL
            WHERE sp.TenSP LIKE ? AND cl.TenCL LIKE ?
        """
        data = ('%' + ten_sp + '%', '%' + ten_cl + '%')
        cursor.execute(sql, data)

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            row = dict(zip(keys, val))
            results.append(row)

        return flask.jsonify(results), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/sanpham/tonkho', methods=['GET'])
def getSanPhamTonKho():
    try:
        cursor = conn.cursor()
        sql = """
            SELECT sp.MaSP, sp.TenSP, sp.MaCL, cl.TenCL, sp.MoTa, sp.GiaNhap, sp.GiaBan, sp.SoLuong
            FROM tblSanPham sp
            INNER JOIN tblChatLieu cl ON sp.MaCL = cl.MaCL
            WHERE sp.SoLuong > 0
        """
        cursor.execute(sql)

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            row = dict(zip(keys, val))
            results.append(row)

        return flask.jsonify(results), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/sanpham/add', methods=['POST'])
def addSanPham():
    try:
        data_json = flask.request.get_json()

        masp = data_json.get("MaSP")
        tensp = data_json.get("TenSP")
        macl = data_json.get("MaCL")
        mota = data_json.get("MoTa")
        gianhap = data_json.get("GiaNhap")
        giaban = data_json.get("GiaBan")
        soluong = data_json.get("SoLuong")

        cursor = conn.cursor()

        cursor.execute("SELECT * FROM tblSanPham WHERE MaSP = ?", masp)
        if cursor.fetchone():
            return flask.jsonify({"mess": "Mã sản phẩm đã tồn tại"}), 400

        cursor.execute("SELECT * FROM tblChatLieu WHERE MaCL = ?", macl)
        if not cursor.fetchone():
            return flask.jsonify({"mess": "Mã chất liệu không tồn tại"}), 404

        sql = """
            INSERT INTO tblSanPham(MaSP, TenSP, MaCL, MoTa, GiaNhap, GiaBan, SoLuong)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        data = (masp, tensp, macl, mota, gianhap, giaban, soluong)

        cursor.execute(sql, data)
        conn.commit()

        return flask.jsonify({"mess": "Thêm sản phẩm thành công"}), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/sanpham/update/<id>', methods=['PUT'])
def updateSanPham(id):
    try:
        data_json = flask.request.get_json()

        tensp = data_json.get("TenSP")
        macl = data_json.get("MaCL")
        mota = data_json.get("MoTa")
        gianhap = data_json.get("GiaNhap")
        giaban = data_json.get("GiaBan")
        soluong = data_json.get("SoLuong")

        cursor = conn.cursor()

        cursor.execute("SELECT * FROM tblChatLieu WHERE MaCL = ?", macl)
        if not cursor.fetchone():
            return flask.jsonify({"mess": "Mã chất liệu không tồn tại"}), 404

        sql = """
            UPDATE tblSanPham
            SET TenSP = ?, MaCL = ?, MoTa = ?, GiaNhap = ?, GiaBan = ?, SoLuong = ?
            WHERE MaSP = ?
        """
        data = (tensp, macl, mota, gianhap, giaban, soluong, id)

        cursor.execute(sql, data)
        conn.commit()

        if cursor.rowcount == 0:
            return flask.jsonify({"mess": "Không tìm thấy sản phẩm để cập nhật"}), 404

        return flask.jsonify({"mess": "Cập nhật sản phẩm thành công"}), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/sanpham/delete/<id>', methods=['DELETE'])
def deleteSanPham(id):
    try:
        cursor = conn.cursor()
        sql = "DELETE FROM tblSanPham WHERE MaSP = ?"

        cursor.execute(sql, id)
        conn.commit()

        if cursor.rowcount == 0:
            return flask.jsonify({"mess": "Không tìm thấy sản phẩm để xóa"}), 404

        return flask.jsonify({"mess": "Xóa sản phẩm thành công"}), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True)