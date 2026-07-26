-- Tạo CSDL
IF DB_ID('DuLieu') IS NULL
BEGIN
    CREATE DATABASE DuLieu;
END
GO

USE DuLieu;
GO

-- Xóa bảng nếu đã tồn tại
IF OBJECT_ID('dbo.tblSanPham', 'U') IS NOT NULL
    DROP TABLE dbo.tblSanPham;
GO

IF OBJECT_ID('dbo.tblChatLieu', 'U') IS NOT NULL
    DROP TABLE dbo.tblChatLieu;
GO

-- Tạo bảng chất liệu
CREATE TABLE tblChatLieu (
    MaCL VARCHAR(10) PRIMARY KEY,
    TenCL NVARCHAR(100) NOT NULL
);
GO

-- Tạo bảng sản phẩm
CREATE TABLE tblSanPham (
    MaSP VARCHAR(10) PRIMARY KEY,
    TenSP NVARCHAR(150) NOT NULL,
    MaCL VARCHAR(10) NOT NULL,
    MoTa NVARCHAR(255) NULL,
    GiaNhap DECIMAL(18,2) NOT NULL,
    GiaBan DECIMAL(18,2) NOT NULL,
    SoLuong INT NOT NULL,
    CONSTRAINT FK_tblSanPham_tblChatLieu
        FOREIGN KEY (MaCL) REFERENCES tblChatLieu(MaCL)
);
GO

-- Dữ liệu mẫu bảng chất liệu
INSERT INTO tblChatLieu (MaCL, TenCL)
VALUES
('CL01', N'Gỗ'),
('CL02', N'Nhựa'),
('CL03', N'Kim loại');
GO

-- Dữ liệu mẫu bảng sản phẩm
INSERT INTO tblSanPham (MaSP, TenSP, MaCL, MoTa, GiaNhap, GiaBan, SoLuong)
VALUES
('SP01', N'Bàn học sinh', 'CL01', N'Bàn gỗ dành cho học sinh', 500000, 750000, 10),
('SP02', N'Ghế nhựa', 'CL02', N'Ghế nhựa cao cấp', 80000, 120000, 25),
('SP03', N'Tủ sắt mini', 'CL03', N'Tủ sắt nhỏ gọn', 900000, 1250000, 0);
GO

-- Kiểm tra dữ liệu
SELECT * FROM tblChatLieu;
SELECT * FROM tblSanPham;
GO