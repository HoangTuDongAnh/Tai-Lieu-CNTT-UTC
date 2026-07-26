-- ============================================================
--  ExpenseDB - Hệ thống Quản lý Chi tiêu Cá nhân
--  Phiên bản FULL sau khi bổ sung:
--  - OTP
--  - Category + Budget theo Tuần/Tháng/Năm
--  - Soft delete cho Categories (IsDeleted)
--  - Support Requests + Support Attachments
--  Database: SQL Server
-- ============================================================

USE master;
GO

-- ============================================================
--  1. TẠO DATABASE NẾU CHƯA TỒN TẠI
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'ExpenseDB')
BEGIN
    CREATE DATABASE ExpenseDB
    COLLATE Vietnamese_CI_AS;
END
GO

USE ExpenseDB;
GO

-- ============================================================
--  2. XÓA CÁC ĐỐI TƯỢNG CŨ NẾU TỒN TẠI
--  Xóa theo thứ tự phụ thuộc khóa ngoại
-- ============================================================
IF OBJECT_ID('dbo.SupportAttachments', 'U') IS NOT NULL DROP TABLE dbo.SupportAttachments;
IF OBJECT_ID('dbo.SupportRequests', 'U') IS NOT NULL DROP TABLE dbo.SupportRequests;
IF OBJECT_ID('dbo.Budgets', 'U') IS NOT NULL DROP TABLE dbo.Budgets;
IF OBJECT_ID('dbo.Transactions', 'U') IS NOT NULL DROP TABLE dbo.Transactions;
IF OBJECT_ID('dbo.Categories', 'U') IS NOT NULL DROP TABLE dbo.Categories;
IF OBJECT_ID('dbo.Wallets', 'U') IS NOT NULL DROP TABLE dbo.Wallets;
IF OBJECT_ID('dbo.UserOTPs', 'U') IS NOT NULL DROP TABLE dbo.UserOTPs;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
GO

-- ============================================================
--  3. BẢNG USERS
--  PK format: U + YYMMDD + 4 số
-- ============================================================
CREATE TABLE dbo.Users (
    UserID          VARCHAR(15)     NOT NULL,
    FullName        NVARCHAR(100)   NOT NULL,
    Email           VARCHAR(150)    NOT NULL,
    PasswordHash    VARCHAR(255)    NOT NULL,
    PhoneNumber     VARCHAR(15)     NULL,
    Avatar          VARCHAR(255)    NULL,
    Role            VARCHAR(20)     NOT NULL DEFAULT 'user',
    Status          VARCHAR(10)     NOT NULL DEFAULT 'active',
    CreatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),
    UpdatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Users PRIMARY KEY (UserID),
    CONSTRAINT UQ_Users_Email UNIQUE (Email),
    CONSTRAINT CK_Users_Role CHECK (Role IN ('user', 'admin')),
    CONSTRAINT CK_Users_Status CHECK (Status IN ('active', 'inactive', 'locked'))
);
GO

-- ============================================================
--  4. BẢNG USER OTPS
-- ============================================================
CREATE TABLE dbo.UserOTPs (
    OtpID        INT IDENTITY(1,1) NOT NULL,
    Email        VARCHAR(150)      NOT NULL,
    OTPCode      VARCHAR(6)        NOT NULL,
    IsUsed       BIT               NOT NULL DEFAULT 0,
    ExpiresAt    DATETIME          NOT NULL,
    CreatedAt    DATETIME          NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_UserOTPs PRIMARY KEY (OtpID)
);
GO

CREATE INDEX IX_UserOTPs_Email_CreatedAt
    ON dbo.UserOTPs(Email, CreatedAt DESC);
GO

-- ============================================================
--  5. BẢNG WALLETS
-- ============================================================
CREATE TABLE dbo.Wallets (
    WalletID        VARCHAR(12)     NOT NULL,
    UserID          VARCHAR(15)     NOT NULL,
    WalletName      NVARCHAR(100)   NOT NULL,
    InitialBalance  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    CurrentBalance  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    Currency        VARCHAR(10)     NOT NULL DEFAULT 'VND',
    IsDefault       BIT             NOT NULL DEFAULT 0,
    CreatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),
    UpdatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Wallets PRIMARY KEY (WalletID),
    CONSTRAINT FK_Wallets_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
        ON DELETE CASCADE,

    CONSTRAINT UQ_Wallets_User_WalletName UNIQUE (UserID, WalletName),
    CONSTRAINT CK_Wallets_InitialBalance CHECK (InitialBalance >= 0),
    CONSTRAINT CK_Wallets_CurrentBalance CHECK (CurrentBalance >= 0)
);
GO

-- ============================================================
--  6. BẢNG CATEGORIES
--  Có bổ sung IsDeleted để hỗ trợ soft delete
-- ============================================================
CREATE TABLE dbo.Categories (
    CategoryID      VARCHAR(15)     NOT NULL,
    UserID          VARCHAR(15)     NULL,
    CategoryName    NVARCHAR(100)   NOT NULL,
    Icon            VARCHAR(50)     NULL,
    Color           VARCHAR(10)     NULL,
    IsDefault       BIT             NOT NULL DEFAULT 0,
    IsDeleted       BIT             NOT NULL DEFAULT 0,
    CreatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),
    UpdatedAt       DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Categories PRIMARY KEY (CategoryID),
    CONSTRAINT FK_Categories_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
        ON DELETE SET NULL,

    CONSTRAINT UQ_Categories_UserName UNIQUE (UserID, CategoryName)
);
GO

-- ============================================================
--  7. BẢNG TRANSACTIONS
-- ============================================================
CREATE TABLE dbo.Transactions (
    TransactionID       VARCHAR(17)     NOT NULL,
    UserID              VARCHAR(15)     NOT NULL,
    WalletID            VARCHAR(12)     NOT NULL,
    CategoryID          VARCHAR(15)     NOT NULL,
    TransactionType     VARCHAR(10)     NOT NULL,      -- expense | income
    Amount              DECIMAL(15,2)   NOT NULL,
    TransactionDate     DATE            NOT NULL,
    Note                NVARCHAR(500)   NULL,
    IsRecurring         BIT             NOT NULL DEFAULT 0,
    RecurInterval       VARCHAR(20)     NULL,
    CreatedAt           DATETIME        NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Transactions PRIMARY KEY (TransactionID),

    CONSTRAINT FK_Transactions_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
        ON DELETE CASCADE,

    CONSTRAINT FK_Transactions_Wallets FOREIGN KEY (WalletID)
        REFERENCES dbo.Wallets(WalletID),

    CONSTRAINT FK_Transactions_Categories FOREIGN KEY (CategoryID)
        REFERENCES dbo.Categories(CategoryID),

    CONSTRAINT CK_Transactions_Type CHECK (TransactionType IN ('expense', 'income')),
    CONSTRAINT CK_Transactions_Amount CHECK (Amount > 0),
    CONSTRAINT CK_Transactions_Recur CHECK (
        RecurInterval IS NULL OR RecurInterval IN ('daily', 'weekly', 'monthly', 'yearly')
    )
);
GO

-- ============================================================
--  8. BẢNG BUDGETS
-- ============================================================
CREATE TABLE dbo.Budgets (
    BudgetID         VARCHAR(13)     NOT NULL,
    UserID           VARCHAR(15)     NOT NULL,
    CategoryID       VARCHAR(15)     NOT NULL,
    LimitAmount      DECIMAL(15,2)   NOT NULL,
    SpentAmount      DECIMAL(15,2)   NOT NULL DEFAULT 0,
    PeriodType       VARCHAR(10)     NOT NULL,     -- week | month | year
    PeriodYear       SMALLINT        NOT NULL,
    PeriodMonth      TINYINT         NULL,
    PeriodWeek       TINYINT         NULL,
    StartDate        DATE            NOT NULL,
    EndDate          DATE            NOT NULL,
    CreatedAt        DATETIME        NOT NULL DEFAULT GETDATE(),
    UpdatedAt        DATETIME        NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Budgets PRIMARY KEY (BudgetID),

    CONSTRAINT FK_Budgets_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
        ON DELETE CASCADE,

    CONSTRAINT FK_Budgets_Categories FOREIGN KEY (CategoryID)
        REFERENCES dbo.Categories(CategoryID),

    CONSTRAINT CK_Budgets_Limit CHECK (LimitAmount > 0),
    CONSTRAINT CK_Budgets_Spent CHECK (SpentAmount >= 0),
    CONSTRAINT CK_Budgets_PeriodType CHECK (PeriodType IN ('week', 'month', 'year')),
    CONSTRAINT CK_Budgets_DateRange CHECK (StartDate <= EndDate),
    CONSTRAINT CK_Budgets_Year CHECK (PeriodYear BETWEEN 2000 AND 9999),
    CONSTRAINT CK_Budgets_Month CHECK (PeriodMonth IS NULL OR PeriodMonth BETWEEN 1 AND 12),
    CONSTRAINT CK_Budgets_Week CHECK (PeriodWeek IS NULL OR PeriodWeek BETWEEN 1 AND 53),

    CONSTRAINT CK_Budgets_PeriodShape CHECK (
        (PeriodType = 'week'  AND PeriodWeek IS NOT NULL AND PeriodMonth IS NULL) OR
        (PeriodType = 'month' AND PeriodMonth IS NOT NULL AND PeriodWeek IS NULL) OR
        (PeriodType = 'year'  AND PeriodMonth IS NULL AND PeriodWeek IS NULL)
    )
);
GO

ALTER TABLE dbo.Budgets
ADD PeriodMonth_NoNull AS ISNULL(PeriodMonth, 0);

ALTER TABLE dbo.Budgets
ADD PeriodWeek_NoNull AS ISNULL(PeriodWeek, 0);

CREATE UNIQUE INDEX UX_Budgets_UserCatPeriod
ON dbo.Budgets (
    UserID,
    CategoryID,
    PeriodType,
    PeriodYear,
    PeriodMonth_NoNull,
    PeriodWeek_NoNull
);
GO

-- ============================================================
--  9. BẢNG SUPPORT REQUESTS
-- ============================================================
CREATE TABLE dbo.SupportRequests (
    SupportRequestID   VARCHAR(18)      NOT NULL,
    UserID             VARCHAR(15)      NOT NULL,
    Subject            NVARCHAR(200)    NOT NULL,
    Message            NVARCHAR(2000)   NOT NULL,
    SupportType        VARCHAR(20)      NOT NULL,
    Priority           VARCHAR(10)      NOT NULL DEFAULT 'medium',
    Status             VARCHAR(20)      NOT NULL DEFAULT 'pending',
    AdminReply         NVARCHAR(2000)   NULL,
    CreatedAt          DATETIME         NOT NULL DEFAULT GETDATE(),
    UpdatedAt          DATETIME         NOT NULL DEFAULT GETDATE(),
    ViewedAt           DATETIME         NULL,
    RepliedAt          DATETIME         NULL,
    ClosedAt           DATETIME         NULL,

    CONSTRAINT PK_SupportRequests PRIMARY KEY (SupportRequestID),

    CONSTRAINT FK_SupportRequests_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
        ON DELETE CASCADE,

    CONSTRAINT CK_SupportRequests_Type CHECK (
        SupportType IN ('bug', 'transaction', 'account', 'feature', 'other')
    ),
    CONSTRAINT CK_SupportRequests_Priority CHECK (
        Priority IN ('low', 'medium', 'high', 'urgent')
    ),
    CONSTRAINT CK_SupportRequests_Status CHECK (
        Status IN ('pending', 'viewed', 'replied', 'closed')
    )
);
GO

-- ============================================================
--  10. BẢNG SUPPORT ATTACHMENTS
-- ============================================================
CREATE TABLE dbo.SupportAttachments (
    AttachmentID       INT IDENTITY(1,1) NOT NULL,
    SupportRequestID   VARCHAR(18)       NOT NULL,
    FileName           NVARCHAR(255)     NOT NULL,
    FileUrl            VARCHAR(500)      NOT NULL,
    FileType           VARCHAR(50)       NULL,
    FileSize           BIGINT            NULL,
    CreatedAt          DATETIME          NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_SupportAttachments PRIMARY KEY (AttachmentID),

    CONSTRAINT FK_SupportAttachments_SupportRequests FOREIGN KEY (SupportRequestID)
        REFERENCES dbo.SupportRequests(SupportRequestID)
        ON DELETE CASCADE
);
GO

-- ============================================================
--  11. INDEX HỖ TRỢ TRUY VẤN THƯỜNG GẶP
-- ============================================================
CREATE INDEX IX_Transactions_User_Date
    ON dbo.Transactions(UserID, TransactionDate DESC);

CREATE INDEX IX_Transactions_Wallet
    ON dbo.Transactions(WalletID);

CREATE INDEX IX_Transactions_Category
    ON dbo.Transactions(CategoryID);

CREATE INDEX IX_Transactions_User_Type_Date
    ON dbo.Transactions(UserID, TransactionType, TransactionDate DESC);

CREATE INDEX IX_Budgets_User_Period
    ON dbo.Budgets(UserID, PeriodType, PeriodYear DESC, PeriodMonth DESC, PeriodWeek DESC);

CREATE INDEX IX_Budgets_User_Category_Period
    ON dbo.Budgets(UserID, CategoryID, PeriodType, PeriodYear DESC, PeriodMonth DESC, PeriodWeek DESC);

CREATE INDEX IX_Budgets_User_Category_DateRange
    ON dbo.Budgets(UserID, CategoryID, StartDate, EndDate);

CREATE INDEX IX_Categories_User
    ON dbo.Categories(UserID);

CREATE INDEX IX_Categories_User_IsDeleted
    ON dbo.Categories(UserID, IsDeleted);

CREATE INDEX IX_SupportRequests_User_CreatedAt
    ON dbo.SupportRequests(UserID, CreatedAt DESC);

CREATE INDEX IX_SupportRequests_Status_CreatedAt
    ON dbo.SupportRequests(Status, CreatedAt DESC);

CREATE INDEX IX_SupportRequests_Type_Priority_CreatedAt
    ON dbo.SupportRequests(SupportType, Priority, CreatedAt DESC);

CREATE INDEX IX_SupportAttachments_Request
    ON dbo.SupportAttachments(SupportRequestID);
GO

-- ============================================================
--  12. STORED PROCEDURE SINH USER ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateUserID
    @NewID VARCHAR(15) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @DatePart VARCHAR(6) = FORMAT(GETDATE(), 'yyMMdd');
    DECLARE @Prefix   VARCHAR(7) = 'U' + @DatePart;
    DECLARE @LastSeq  INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(UserID, 4) AS INT))
    FROM dbo.Users
    WHERE UserID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('0000' + CAST(@LastSeq AS VARCHAR), 4);
END;
GO

-- ============================================================
--  13. STORED PROCEDURE SINH WALLET ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateWalletID
    @UserID  VARCHAR(15),
    @NewID   VARCHAR(12) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @UserSuffix VARCHAR(4) = RIGHT(@UserID, 4);
    DECLARE @Prefix     VARCHAR(5) = 'W' + @UserSuffix;
    DECLARE @LastSeq    INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(WalletID, 4) AS INT))
    FROM dbo.Wallets
    WHERE WalletID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('0000' + CAST(@LastSeq AS VARCHAR), 4);
END;
GO

-- ============================================================
--  14. STORED PROCEDURE SINH CATEGORY ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateCategoryID
    @NewID VARCHAR(15) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @DatePart VARCHAR(6) = FORMAT(GETDATE(), 'yyMMdd');
    DECLARE @Prefix   VARCHAR(9) = 'CAT' + @DatePart;
    DECLARE @LastSeq  INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(CategoryID, 3) AS INT))
    FROM dbo.Categories
    WHERE CategoryID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('000' + CAST(@LastSeq AS VARCHAR), 3);
END;
GO

-- ============================================================
--  15. STORED PROCEDURE SINH TRANSACTION ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateTransactionID
    @NewID VARCHAR(17) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @DatePart VARCHAR(6) = FORMAT(GETDATE(), 'yyMMdd');
    DECLARE @Prefix   VARCHAR(9) = 'TXN' + @DatePart;
    DECLARE @LastSeq  INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(TransactionID, 4) AS INT))
    FROM dbo.Transactions
    WHERE TransactionID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('0000' + CAST(@LastSeq AS VARCHAR), 4);
END;
GO

-- ============================================================
--  16. STORED PROCEDURE SINH BUDGET ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateBudgetID
    @NewID VARCHAR(13) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @MonthPart VARCHAR(4) = FORMAT(GETDATE(), 'yyMM');
    DECLARE @Prefix    VARCHAR(7) = 'BUD' + @MonthPart;
    DECLARE @LastSeq   INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(BudgetID, 4) AS INT))
    FROM dbo.Budgets
    WHERE BudgetID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('0000' + CAST(@LastSeq AS VARCHAR), 4);
END;
GO

-- ============================================================
--  17. STORED PROCEDURE SINH SUPPORT REQUEST ID
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GenerateSupportRequestID
    @NewID VARCHAR(18) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @DatePart VARCHAR(6) = FORMAT(GETDATE(), 'yyMMdd');
    DECLARE @Prefix   VARCHAR(9) = 'SUP' + @DatePart;
    DECLARE @LastSeq  INT;

    SELECT @LastSeq = MAX(CAST(RIGHT(SupportRequestID, 4) AS INT))
    FROM dbo.SupportRequests
    WHERE SupportRequestID LIKE @Prefix + '%';

    SET @LastSeq = ISNULL(@LastSeq, 0) + 1;
    SET @NewID   = @Prefix + RIGHT('0000' + CAST(@LastSeq AS VARCHAR), 4);
END;
GO

-- ============================================================
--  18. HÀM LẤY NGÀY ĐẦU TUẦN ISO (THỨ HAI)
-- ============================================================
CREATE OR ALTER FUNCTION dbo.fn_GetIsoWeekStartDate (
    @Year INT,
    @IsoWeek INT
)
RETURNS DATE
AS
BEGIN
    DECLARE @Jan4 DATE = DATEFROMPARTS(@Year, 1, 4);
    DECLARE @Weekday INT = (DATEDIFF(DAY, '19000101', @Jan4) % 7 + 7) % 7; -- Monday = 0
    DECLARE @FirstIsoMonday DATE = DATEADD(DAY, -@Weekday, @Jan4);
    RETURN DATEADD(WEEK, @IsoWeek - 1, @FirstIsoMonday);
END;
GO

-- ============================================================
--  19. PROC ĐỒNG BỘ SPENTAMOUNT CHO 1 CATEGORY TẠI 1 NGÀY
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_SyncBudgetsByTransactionDate
    @UserID          VARCHAR(15),
    @CategoryID      VARCHAR(15),
    @TransactionDate DATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE b
    SET
        SpentAmount = x.TotalExpense,
        UpdatedAt   = GETDATE()
    FROM dbo.Budgets b
    CROSS APPLY (
        SELECT ISNULL(SUM(t.Amount), 0) AS TotalExpense
        FROM dbo.Transactions t
        WHERE t.UserID = b.UserID
          AND t.CategoryID = b.CategoryID
          AND t.TransactionType = 'expense'
          AND t.TransactionDate BETWEEN b.StartDate AND b.EndDate
    ) x
    WHERE b.UserID = @UserID
      AND b.CategoryID = @CategoryID
      AND @TransactionDate BETWEEN b.StartDate AND b.EndDate;
END;
GO

-- ============================================================
--  20. PROC TÍNH LẠI SPENTAMOUNT TOÀN BỘ 1 BUDGET
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_RecalculateBudgetSpent
    @BudgetID VARCHAR(13)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE b
    SET
        SpentAmount = x.TotalExpense,
        UpdatedAt   = GETDATE()
    FROM dbo.Budgets b
    CROSS APPLY (
        SELECT ISNULL(SUM(t.Amount), 0) AS TotalExpense
        FROM dbo.Transactions t
        WHERE t.UserID = b.UserID
          AND t.CategoryID = b.CategoryID
          AND t.TransactionType = 'expense'
          AND t.TransactionDate BETWEEN b.StartDate AND b.EndDate
    ) x
    WHERE b.BudgetID = @BudgetID;
END;
GO

-- ============================================================
--  21. PROC TẠO BUDGET MỚI THEO WEEK/MONTH/YEAR
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_CreateBudget
    @UserID       VARCHAR(15),
    @CategoryID   VARCHAR(15),
    @LimitAmount  DECIMAL(15,2),
    @PeriodType   VARCHAR(10),
    @PeriodYear   SMALLINT,
    @PeriodMonth  TINYINT = NULL,
    @PeriodWeek   TINYINT = NULL,
    @NewBudgetID  VARCHAR(13) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @StartDate DATE;
    DECLARE @EndDate   DATE;

    IF @PeriodType NOT IN ('week', 'month', 'year')
        THROW 50010, N'PeriodType phải là week, month hoặc year.', 1;

    IF @LimitAmount <= 0
        THROW 50011, N'LimitAmount phải lớn hơn 0.', 1;

    IF @PeriodType = 'week'
    BEGIN
        IF @PeriodWeek IS NULL OR @PeriodWeek NOT BETWEEN 1 AND 53 OR @PeriodMonth IS NOT NULL
            THROW 50012, N'Budget theo tuần yêu cầu PeriodWeek hợp lệ và PeriodMonth phải NULL.', 1;

        SET @StartDate = dbo.fn_GetIsoWeekStartDate(@PeriodYear, @PeriodWeek);
        SET @EndDate   = DATEADD(DAY, 6, @StartDate);
    END
    ELSE IF @PeriodType = 'month'
    BEGIN
        IF @PeriodMonth IS NULL OR @PeriodMonth NOT BETWEEN 1 AND 12 OR @PeriodWeek IS NOT NULL
            THROW 50013, N'Budget theo tháng yêu cầu PeriodMonth hợp lệ và PeriodWeek phải NULL.', 1;

        SET @StartDate = DATEFROMPARTS(@PeriodYear, @PeriodMonth, 1);
        SET @EndDate   = EOMONTH(@StartDate);
    END
    ELSE
    BEGIN
        IF @PeriodMonth IS NOT NULL OR @PeriodWeek IS NOT NULL
            THROW 50014, N'Budget theo năm không dùng PeriodMonth và PeriodWeek.', 1;

        SET @StartDate = DATEFROMPARTS(@PeriodYear, 1, 1);
        SET @EndDate   = DATEFROMPARTS(@PeriodYear, 12, 31);
    END

    EXEC dbo.sp_GenerateBudgetID @NewID = @NewBudgetID OUTPUT;

    INSERT INTO dbo.Budgets (
        BudgetID, UserID, CategoryID, LimitAmount, SpentAmount,
        PeriodType, PeriodYear, PeriodMonth, PeriodWeek,
        StartDate, EndDate
    )
    VALUES (
        @NewBudgetID, @UserID, @CategoryID, @LimitAmount, 0,
        @PeriodType, @PeriodYear, @PeriodMonth, @PeriodWeek,
        @StartDate, @EndDate
    );

    EXEC dbo.sp_RecalculateBudgetSpent @BudgetID = @NewBudgetID;
END;
GO

-- ============================================================
--  22. PROCEDURE THÊM GIAO DỊCH
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_CreateTransaction
    @UserID          VARCHAR(15),
    @WalletID        VARCHAR(12),
    @CategoryID      VARCHAR(15),
    @TransactionType VARCHAR(10),
    @Amount          DECIMAL(15,2),
    @TransactionDate DATE,
    @Note            NVARCHAR(500) = NULL,
    @IsRecurring     BIT = 0,
    @RecurInterval   VARCHAR(20) = NULL,
    @NewTransID      VARCHAR(17) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        EXEC dbo.sp_GenerateTransactionID @NewID = @NewTransID OUTPUT;

        INSERT INTO dbo.Transactions (
            TransactionID, UserID, WalletID, CategoryID,
            TransactionType, Amount, TransactionDate,
            Note, IsRecurring, RecurInterval
        )
        VALUES (
            @NewTransID, @UserID, @WalletID, @CategoryID,
            @TransactionType, @Amount, @TransactionDate,
            @Note, @IsRecurring, @RecurInterval
        );

        IF @TransactionType = 'income'
        BEGIN
            UPDATE dbo.Wallets
            SET CurrentBalance = CurrentBalance + @Amount,
                UpdatedAt = GETDATE()
            WHERE WalletID = @WalletID;
        END
        ELSE
        BEGIN
            UPDATE dbo.Wallets
            SET CurrentBalance = CurrentBalance - @Amount,
                UpdatedAt = GETDATE()
            WHERE WalletID = @WalletID;
        END

        IF @TransactionType = 'expense'
        BEGIN
            EXEC dbo.sp_SyncBudgetsByTransactionDate
                @UserID = @UserID,
                @CategoryID = @CategoryID,
                @TransactionDate = @TransactionDate;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ============================================================
--  23. PROCEDURE XÓA GIAO DỊCH
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_DeleteTransaction
    @TransactionID VARCHAR(17),
    @UserID        VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @WalletID VARCHAR(12);
        DECLARE @CatID    VARCHAR(15);
        DECLARE @Type     VARCHAR(10);
        DECLARE @Amount   DECIMAL(15,2);
        DECLARE @TxDate   DATE;

        SELECT
            @WalletID = WalletID,
            @CatID    = CategoryID,
            @Type     = TransactionType,
            @Amount   = Amount,
            @TxDate   = TransactionDate
        FROM dbo.Transactions
        WHERE TransactionID = @TransactionID
          AND UserID        = @UserID;

        IF @@ROWCOUNT = 0
            THROW 50001, N'Giao dịch không tồn tại hoặc không có quyền xóa.', 1;

        IF @Type = 'income'
        BEGIN
            UPDATE dbo.Wallets
            SET CurrentBalance = CurrentBalance - @Amount,
                UpdatedAt = GETDATE()
            WHERE WalletID = @WalletID;
        END
        ELSE
        BEGIN
            UPDATE dbo.Wallets
            SET CurrentBalance = CurrentBalance + @Amount,
                UpdatedAt = GETDATE()
            WHERE WalletID = @WalletID;
        END

        DELETE FROM dbo.Transactions
        WHERE TransactionID = @TransactionID
          AND UserID        = @UserID;

        IF @Type = 'expense'
        BEGIN
            EXEC dbo.sp_SyncBudgetsByTransactionDate
                @UserID = @UserID,
                @CategoryID = @CatID,
                @TransactionDate = @TxDate;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ============================================================
--  24. PROCEDURE BÁO CÁO TỔNG THU/CHI THEO THÁNG
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GetMonthlySummary
    @UserID VARCHAR(15),
    @Year   SMALLINT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        MONTH(TransactionDate) AS [Month],
        SUM(CASE WHEN TransactionType = 'income'  THEN Amount ELSE 0 END) AS TotalIncome,
        SUM(CASE WHEN TransactionType = 'expense' THEN Amount ELSE 0 END) AS TotalExpense
    FROM dbo.Transactions
    WHERE UserID = @UserID
      AND YEAR(TransactionDate) = @Year
    GROUP BY MONTH(TransactionDate)
    ORDER BY [Month];
END;
GO

-- ============================================================
--  25. PROCEDURE BÁO CÁO CHI TIÊU THEO DANH MỤC
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GetCategorySummary
    @UserID VARCHAR(15),
    @Month  TINYINT,
    @Year   SMALLINT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        c.CategoryID,
        c.CategoryName,
        c.Icon,
        c.Color,
        SUM(t.Amount) AS TotalAmount,
        CAST(
            SUM(t.Amount) * 100.0
            / NULLIF(SUM(SUM(t.Amount)) OVER (), 0)
            AS DECIMAL(5,2)
        ) AS Percentage
    FROM dbo.Transactions t
    INNER JOIN dbo.Categories c
        ON t.CategoryID = c.CategoryID
    WHERE t.UserID = @UserID
      AND t.TransactionType = 'expense'
      AND c.IsDeleted = 0
      AND MONTH(t.TransactionDate) = @Month
      AND YEAR(t.TransactionDate)  = @Year
    GROUP BY c.CategoryID, c.CategoryName, c.Icon, c.Color
    ORDER BY TotalAmount DESC;
END;
GO

-- ============================================================
--  26. PROCEDURE LẤY DỮ LIỆU DASHBOARD TỔNG QUAN
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.sp_GetDashboardOverview
    @UserID VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @ThisMonth TINYINT  = MONTH(GETDATE());
    DECLARE @ThisYear  SMALLINT = YEAR(GETDATE());

    SELECT
        (SELECT ISNULL(SUM(CurrentBalance), 0)
         FROM dbo.Wallets
         WHERE UserID = @UserID) AS TotalBalance,

        (SELECT ISNULL(SUM(Amount), 0)
         FROM dbo.Transactions
         WHERE UserID = @UserID
           AND TransactionType = 'income'
           AND MONTH(TransactionDate) = @ThisMonth
           AND YEAR(TransactionDate)  = @ThisYear) AS MonthlyIncome,

        (SELECT ISNULL(SUM(Amount), 0)
         FROM dbo.Transactions
         WHERE UserID = @UserID
           AND TransactionType = 'expense'
           AND MONTH(TransactionDate) = @ThisMonth
           AND YEAR(TransactionDate)  = @ThisYear) AS MonthlyExpense,

        (SELECT COUNT(*)
         FROM dbo.Transactions
         WHERE UserID = @UserID
           AND MONTH(TransactionDate) = @ThisMonth
           AND YEAR(TransactionDate)  = @ThisYear) AS TransactionCount;
END;
GO

-- ============================================================
--  27. VIEW PHỤC VỤ UI CATEGORY + BUDGET
-- ============================================================
CREATE OR ALTER VIEW dbo.vw_CategoryBudgetOverview
AS
SELECT
    c.CategoryID,
    c.UserID,
    c.CategoryName,
    c.Icon,
    c.Color,
    c.IsDefault,
    c.IsDeleted,
    b.BudgetID,
    b.LimitAmount,
    b.SpentAmount,
    b.PeriodType,
    b.PeriodYear,
    b.PeriodMonth,
    b.PeriodWeek,
    b.StartDate,
    b.EndDate,
    CASE
        WHEN b.BudgetID IS NULL THEN NULL
        ELSE b.LimitAmount - b.SpentAmount
    END AS RemainingAmount,
    CASE
        WHEN b.BudgetID IS NULL OR b.LimitAmount = 0 THEN NULL
        ELSE CAST((b.SpentAmount * 100.0) / b.LimitAmount AS DECIMAL(5,2))
    END AS PercentageUsed,
    CASE
        WHEN b.BudgetID IS NULL THEN 'none'
        WHEN b.SpentAmount < b.LimitAmount THEN 'normal'
        WHEN b.SpentAmount = b.LimitAmount THEN 'reached'
        ELSE 'over'
    END AS BudgetStatus
FROM dbo.Categories c
LEFT JOIN dbo.Budgets b
    ON c.CategoryID = b.CategoryID;
GO

-- ============================================================
--  28. SEED DATA - DANH MỤC MẶC ĐỊNH HỆ THỐNG
-- ============================================================
INSERT INTO dbo.Categories (CategoryID, UserID, CategoryName, Icon, Color, IsDefault, IsDeleted)
VALUES
    ('CAT000000001', NULL, N'Ăn uống',      'bx-restaurant',      '#D85A30', 1, 0),
    ('CAT000000002', NULL, N'Đi lại',       'bx-car',             '#185FA5', 1, 0),
    ('CAT000000003', NULL, N'Mua sắm',      'bx-shopping-bag',    '#993556', 1, 0),
    ('CAT000000004', NULL, N'Giải trí',     'bx-game',            '#534AB7', 1, 0),
    ('CAT000000005', NULL, N'Sức khỏe',     'bx-plus-medical',    '#0F6E56', 1, 0),
    ('CAT000000006', NULL, N'Giáo dục',     'bx-book-open',       '#854F0B', 1, 0),
    ('CAT000000007', NULL, N'Hóa đơn',      'bx-home',            '#5F5E5A', 1, 0),
    ('CAT000000008', NULL, N'Tiết kiệm',    'bx-wallet',          '#3B6D11', 1, 0),
    ('CAT000000009', NULL, N'Du lịch',      'bx-plane',           '#1D9E75', 1, 0),
    ('CAT000000010', NULL, N'Khác',         'bx-dots-horizontal', '#888780', 1, 0);
GO

-- ============================================================
--  29. SEED DATA - TÀI KHOẢN ADMIN MẪU
-- ============================================================
DECLARE @AdminID VARCHAR(15) = 'U2503290001';

INSERT INTO dbo.Users (UserID, FullName, Email, PasswordHash, Role, Status)
VALUES (
    @AdminID,
    N'Quản trị viên',
    'admin@expenseapp.vn',
    '$2b$12$LQv3c1yqBWVHxkd0LQ1XJeWbzVLILbK/7A8YBz6mO4mFRtXqM8G..',
    'admin',
    'active'
);
GO

DECLARE @WalletID VARCHAR(12);
EXEC dbo.sp_GenerateWalletID @UserID = 'U2503290001', @NewID = @WalletID OUTPUT;

INSERT INTO dbo.Wallets (WalletID, UserID, WalletName, InitialBalance, CurrentBalance, IsDefault)
VALUES (@WalletID, 'U2503290001', N'Tiền mặt', 5000000, 5000000, 1);
GO

DECLARE @BudgetID1 VARCHAR(13), @BudgetID2 VARCHAR(13), @BudgetID3 VARCHAR(13);
EXEC dbo.sp_CreateBudget
    @UserID='U2503290001',
    @CategoryID='CAT000000001',
    @LimitAmount=2000000,
    @PeriodType='month',
    @PeriodYear=2026,
    @PeriodMonth=4,
    @NewBudgetID=@BudgetID1 OUTPUT;

EXEC dbo.sp_CreateBudget
    @UserID='U2503290001',
    @CategoryID='CAT000000001',
    @LimitAmount=600000,
    @PeriodType='week',
    @PeriodYear=2026,
    @PeriodWeek=15,
    @NewBudgetID=@BudgetID2 OUTPUT;

EXEC dbo.sp_CreateBudget
    @UserID='U2503290001',
    @CategoryID='CAT000000001',
    @LimitAmount=24000000,
    @PeriodType='year',
    @PeriodYear=2026,
    @NewBudgetID=@BudgetID3 OUTPUT;
GO

-- ============================================================
--  30. KIỂM TRA DỮ LIỆU KHỞI TẠO
-- ============================================================
SELECT 'Users' AS TableName, COUNT(*) AS Records FROM dbo.Users
UNION ALL
SELECT 'UserOTPs', COUNT(*) FROM dbo.UserOTPs
UNION ALL
SELECT 'Wallets', COUNT(*) FROM dbo.Wallets
UNION ALL
SELECT 'Categories', COUNT(*) FROM dbo.Categories
UNION ALL
SELECT 'Transactions', COUNT(*) FROM dbo.Transactions
UNION ALL
SELECT 'Budgets', COUNT(*) FROM dbo.Budgets
UNION ALL
SELECT 'SupportRequests', COUNT(*) FROM dbo.SupportRequests
UNION ALL
SELECT 'SupportAttachments', COUNT(*) FROM dbo.SupportAttachments;
GO

-- ============================================================
--  31. THÔNG BÁO HOÀN TẤT
-- ============================================================
PRINT N'✅ ExpenseDB phiên bản FULL đã được tạo thành công!';
PRINT N'   - UserOTPs phục vụ xác thực email bằng OTP';
PRINT N'   - Budgets hỗ trợ PeriodType = week | month | year';
PRINT N'   - Categories hỗ trợ soft delete bằng IsDeleted';
PRINT N'   - SupportRequests + SupportAttachments đã sẵn sàng';
PRINT N'   - Transaction expense sẽ tự động đồng bộ budget liên quan';
PRINT N'   - Có view vw_CategoryBudgetOverview để phục vụ overview';
GO
