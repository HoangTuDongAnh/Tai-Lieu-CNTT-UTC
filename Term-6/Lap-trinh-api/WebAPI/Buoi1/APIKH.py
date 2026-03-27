import flask
import pyodbc

app = flask.Flask(__name__)

conn_str = (
    "DRIVER={SQL Server};"
    "SERVER=.\\SQLEXPRESS;"
    "DATABASE=BKCAD_KhachHang;"
    "Trusted_Connection=yes;"
)

conn = pyodbc.connect(conn_str)


@app.route('/kh/getall', methods=['GET'])
def getAllKH():
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM tblQLKH")

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            results.append(dict(zip(keys, val)))

        resp = flask.jsonify(results)
        resp.status_code = 200
        return resp

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/kh/getbyid/<id>', methods=['GET'])
def getKHById(id):
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM tblQLKH WHERE maKhach = ?", id)

        results = []
        keys = [i[0] for i in cursor.description]

        for val in cursor.fetchall():
            results.append(dict(zip(keys, val)))

        resp = flask.jsonify(results)
        resp.status_code = 200
        return resp

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/kh/add', methods=['POST'])
def addKH():
    try:
        mk = flask.request.json.get("makhach")
        tk = flask.request.json.get("tenKhach")
        dc = flask.request.json.get("diaChi")
        dt = flask.request.json.get("dienThoai")

        cursor = conn.cursor()
        sql = "INSERT INTO tblQLKH(makhach, tenKhach, diaChi, dienThoai) VALUES (?, ?, ?, ?)"
        data = (mk, tk, dc, dt)

        cursor.execute(sql, data)
        conn.commit()

        resp = flask.jsonify({"mess": "thành công"})
        resp.status_code = 200
        return resp

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


@app.route('/kh/update', methods=['PUT'])
def updateKH():
    try:
        ma = flask.request.json.get("makhach")
        tk = flask.request.json.get("tenKhach")
        dc = flask.request.json.get("diaChi")
        dt = flask.request.json.get("dienThoai")

        cursor = conn.cursor()
        sql = "UPDATE tblQLKH SET tenKhach = ?, diaChi = ?, dienThoai = ? WHERE makhach = ?"
        data = (tk, dc, dt, ma)

        cursor.execute(sql, data)
        conn.commit()

        resp = flask.jsonify({"mess": "thành công"})
        resp.status_code = 200
        return resp

    except Exception as e:
        return flask.jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True)