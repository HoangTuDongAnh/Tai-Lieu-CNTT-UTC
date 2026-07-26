import flask
import pyodbc
from flask_cors import CORS

app = flask.Flask(__name__)
CORS(app)

conn_str = (
    "DRIVER={SQL Server};"
    "SERVER=HOANGNGUYEN\\SQLEXPRESS;"
    "DATABASE=QLGiangVienDB;"
    "Trusted_Connection=yes;"
)

conn = pyodbc.connect(conn_str)


@app.route('/gv/search', methods=['GET'])
def searchGV():
    try:
        gioiTinh = flask.request.args.get("gioiTinh", "")
        tenBoMon = flask.request.args.get("tenBoMon", "")

        cursor = conn.cursor()

        sql = """
            SELECT 
                gv.MaGV,
                gv.TenGV,
                gv.GioiTinh,
                gv.DiaChi,
                gv.SoDT,
                bm.MaBM,
                bm.TenBM
            FROM tblGV gv
            INNER JOIN tblBoMon bm ON gv.BoMon = bm.MaBM
            WHERE 1 = 1
        """

        params = []

        if gioiTinh.strip() != "":
            sql += " AND gv.GioiTinh LIKE ?"
            params.append(f"%{gioiTinh}%")

        if tenBoMon.strip() != "":
            sql += " AND bm.TenBM LIKE ?"
            params.append(f"%{tenBoMon}%")

        cursor.execute(sql, params)

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            results.append(dict(zip(keys, val)))

        return flask.jsonify(results), 200

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True, port=5000)