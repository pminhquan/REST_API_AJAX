IF DB_ID(N'jakartaJPA') IS NULL
BEGIN
    CREATE DATABASE jakartaJPA;
END;
GO

USE jakartaJPA;
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;
BEGIN TRY

    -- 1. If users table already exists: safe idempotent schema adjustment for Role
    IF OBJECT_ID(N'users', N'U') IS NOT NULL
    BEGIN
        DECLARE @roleColId INT = COLUMNPROPERTY(OBJECT_ID(N'users'), N'Role', 'ColumnId');

        -- If Role column exists, inspect existing data before making any schema changes
        IF @roleColId IS NOT NULL
        BEGIN
            -- Fail-closed validation: check for NULL, empty, or unknown Role values
            DECLARE @invalidRoleCount INT = 0;
            SELECT @invalidRoleCount = COUNT(*)
            FROM users
            WHERE Role IS NULL OR UPPER(LTRIM(RTRIM(CONVERT(NVARCHAR(MAX), Role)))) NOT IN (N'ADMIN', N'CUSTOMER');

            IF @invalidRoleCount > 0
            BEGIN
                DECLARE @errorMsg NVARCHAR(2048) =
                    N'MIGRATION FAILED: Table [users] contains ' + CAST(@invalidRoleCount AS NVARCHAR(10))
                    + N' row(s) with NULL or unmappable Role values. '
                    + N'Migration aborted to prevent data corruption. '
                    + N'Operator guidance: Inspect unmappable records with: '
                    + N'SELECT Id, Username, Email, Role FROM users WHERE Role IS NULL OR UPPER(LTRIM(RTRIM(CONVERT(NVARCHAR(MAX), Role)))) NOT IN (''ADMIN'', ''CUSTOMER''); '
                    + N'Please assign valid roles (ADMIN or CUSTOMER) manually before rerunning this migration.';
                THROW 51000, @errorMsg, 1;
            END;

            -- Data is valid: canonicalize casing and trim whitespace
            EXEC sp_executesql N'UPDATE users SET Role = UPPER(LTRIM(RTRIM(CONVERT(NVARCHAR(MAX), Role))));';

            -- Check if an existing check constraint already enforces (Role IN ('ADMIN','CUSTOMER'))
            DECLARE @exactCheckExists BIT = 0;

            IF EXISTS (
                SELECT 1
                FROM sys.check_constraints cc
                WHERE cc.parent_object_id = OBJECT_ID(N'users')
                  AND (
                      cc.parent_column_id = @roleColId
                      OR cc.definition LIKE N'%[[]Role]%'
                      OR cc.definition LIKE N'%(Role%'
                  )
                  AND (
                      UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%ROLEIN(%''ADMIN'',''CUSTOMER''%)%' 
                      OR UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%ROLEIN(N''ADMIN'',N''CUSTOMER'')%'
                      OR UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%(ROLE=''ADMIN''ORROLE=''CUSTOMER'')%'
                      OR UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%(ROLE=''CUSTOMER''ORROLE=''ADMIN'')%'
                      OR UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%(ROLE=N''ADMIN''ORROLE=N''CUSTOMER'')%'
                      OR UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ISNULL(cc.definition, N''), '[', ''), ']', ''), ' ', ''), CHAR(9), ''), CHAR(10), ''), CHAR(13), '')) LIKE N'%(ROLE=N''CUSTOMER''ORROLE=N''ADMIN'')%'
                  )
            )
            BEGIN
                SET @exactCheckExists = 1;
            END;

            -- Alter column type to NVARCHAR(20) NOT NULL if not already
            IF EXISTS (
                SELECT 1 FROM sys.columns c
                JOIN sys.types t ON c.user_type_id = t.user_type_id
                WHERE c.object_id = OBJECT_ID(N'users')
                  AND c.name = N'Role'
                  AND (c.is_nullable = 1 OR t.name <> 'nvarchar' OR c.max_length <> 40)
            )
            BEGIN
                ALTER TABLE users ALTER COLUMN Role NVARCHAR(20) NOT NULL;
            END;

            -- Ensure default constraint exists
            IF NOT EXISTS (
                SELECT 1 FROM sys.default_constraints dc
                WHERE dc.parent_object_id = OBJECT_ID(N'users')
                  AND dc.parent_column_id = @roleColId
            )
            BEGIN
                ALTER TABLE users ADD CONSTRAINT DF_users_Role DEFAULT N'CUSTOMER' FOR Role;
            END;

            -- Ensure check constraint exists (preserve every unknown/additional constraint: NEVER DROP any constraint)
            IF @exactCheckExists = 0 AND NOT EXISTS (
                SELECT 1 FROM sys.check_constraints
                WHERE parent_object_id = OBJECT_ID(N'users') AND name = N'CK_users_Role'
            )
            BEGIN
                ALTER TABLE users WITH CHECK ADD CONSTRAINT CK_users_Role CHECK (Role IN ('ADMIN', 'CUSTOMER'));
            END;
        END
        ELSE
        BEGIN
            -- Role column is missing. Check if users table already has data.
            IF EXISTS (SELECT 1 FROM users)
            BEGIN
                DECLARE @noRoleError NVARCHAR(2048) =
                    N'MIGRATION FAILED: Table [users] has existing rows but lacks the [Role] column. '
                    + N'Migration aborted to prevent unverified default assignment. '
                    + N'Operator guidance: Add [Role] column and populate with valid values (ADMIN or CUSTOMER) for all users before re-running.';
                THROW 51002, @noRoleError, 1;
            END
            ELSE
            BEGIN
                ALTER TABLE users ADD Role NVARCHAR(20) NOT NULL CONSTRAINT DF_users_Role DEFAULT N'CUSTOMER' CONSTRAINT CK_users_Role CHECK (Role IN ('ADMIN', 'CUSTOMER'));
            END;
        END;
    END;

    -- 2. Create tables if they do not exist
    IF OBJECT_ID(N'categories', N'U') IS NULL
    BEGIN
        CREATE TABLE categories
        (
            CategoryId INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_categories PRIMARY KEY,
            CategoryName NVARCHAR(100) NOT NULL,
            Images NVARCHAR(500) NULL,
            Status INT NULL
        );
    END;

    IF OBJECT_ID(N'users', N'U') IS NULL
    BEGIN
        CREATE TABLE users
        (
            Id INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_users PRIMARY KEY,
            Username NVARCHAR(50) NOT NULL,
            Email NVARCHAR(100) NOT NULL,
            PasswordHash NVARCHAR(255) NOT NULL,
            Active BIT NOT NULL,
            CreatedAt DATETIME2(6) NOT NULL,
            Fullname NVARCHAR(100) NULL,
            Phone NVARCHAR(30) NULL,
            Images NVARCHAR(500) NULL,
            Role NVARCHAR(20) NOT NULL
                CONSTRAINT DF_users_Role DEFAULT N'CUSTOMER'
                CONSTRAINT CK_users_Role CHECK (Role IN ('ADMIN','CUSTOMER')),
            CONSTRAINT UQ_users_Username UNIQUE (Username),
            CONSTRAINT UQ_users_Email UNIQUE (Email)
        );
    END;

    IF OBJECT_ID(N'products', N'U') IS NULL
    BEGIN
        CREATE TABLE products
        (
            ProductId INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_products PRIMARY KEY,
            ProductName NVARCHAR(250) NOT NULL,
            Description NVARCHAR(500) NULL,
            Price FLOAT(53) NULL,
            Images NVARCHAR(500) NULL,
            Status INT NULL,
            CategoryId INT NOT NULL,
            CreatedAt DATETIME2(6) NULL,
            CONSTRAINT FK_products_categories
                FOREIGN KEY (CategoryId) REFERENCES categories (CategoryId)
        );
    END;

    IF OBJECT_ID(N'otp_tokens', N'U') IS NULL
    BEGIN
        CREATE TABLE otp_tokens
        (
            Id INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_otp_tokens PRIMARY KEY,
            UserId INT NOT NULL,
            Purpose NVARCHAR(50) NOT NULL,
            CodeHash NVARCHAR(255) NOT NULL,
            ExpiresAt DATETIME2(6) NOT NULL,
            Attempts INT NOT NULL,
            Used BIT NOT NULL,
            CreatedAt DATETIME2(6) NOT NULL,
            CONSTRAINT FK_otp_tokens_users
                FOREIGN KEY (UserId) REFERENCES users (Id)
        );
    END;

        -- Seed default admin account
    IF NOT EXISTS (SELECT 1 FROM users WHERE Username = N'test_admin')
    BEGIN
        INSERT INTO users
        (
            Username,
            Email,
            PasswordHash,
            Active,
            CreatedAt,
            Fullname,
            Phone,
            Images,
            Role
        )
        VALUES
        (
            N'test_admin',
            N'admin@test.com',
            N'$2a$10$jnW/TplZDEnWQOkQCHmaou8.N0JDjUJcSvSBH5SipNaLPPlm8xxvi',
            1,
            GETDATE(),
            N'Administrator',
            NULL,
            NULL,
            N'ADMIN'
        );
    END;

    -- 3. Seed data idempotently
    IF NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = N'Electronics')
    BEGIN
        INSERT INTO categories (CategoryName, Images, Status)
        VALUES (N'Electronics', N'electronics.jpg', 1);
    END
    ELSE
    BEGIN
        UPDATE categories
        SET Images = N'electronics.jpg'
        WHERE CategoryName = N'Electronics' AND (Images IS NULL OR LTRIM(RTRIM(Images)) = N'');
    END;

    DECLARE @ElectronicsCatId INT;
    SELECT TOP 1 @ElectronicsCatId = CategoryId FROM categories WHERE CategoryName = N'Electronics';

    IF @ElectronicsCatId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'iPhone 15 Pro')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'iPhone 15 Pro', N'Premium smartphone with a titanium design.', 28990000, N'iphone15.jpg', 1, @ElectronicsCatId, '2026-09-05T10:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Samsung Galaxy S25')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'Samsung Galaxy S25', N'Flagship Android smartphone with a bright display.', 23990000, N'samsung-s25.jpg', 1, @ElectronicsCatId, '2026-09-05T09:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'MacBook Air M3')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'MacBook Air M3', N'Lightweight laptop powered by Apple silicon.', 27990000, N'macbook-air-m3.jpg', 1, @ElectronicsCatId, '2026-09-05T08:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Dell XPS 13')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'Dell XPS 13', N'Compact premium Windows ultrabook.', 24990000, N'dell-xps13.jpg', 1, @ElectronicsCatId, '2026-09-05T07:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'AirPods Pro 2')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'AirPods Pro 2', N'Wireless earbuds with active noise cancellation.', 5990000, N'airpods-pro.jpg', 1, @ElectronicsCatId, '2026-09-05T06:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'iPad Air M2')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'iPad Air M2', N'Versatile tablet for work, study, and entertainment.', 16990000, N'ipad-air-m2.jpg', 1, @ElectronicsCatId, '2026-09-05T05:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'ASUS ROG Strix')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'ASUS ROG Strix', N'Performance gaming laptop with a dedicated GPU.', 32990000, N'rog-strix.jpg', 1, @ElectronicsCatId, '2026-09-05T04:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Apple Watch Series 10')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'Apple Watch Series 10', N'Smartwatch with fitness and health tracking.', 10990000, N'apple-watch.jpg', 1, @ElectronicsCatId, '2026-09-05T03:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Sony WH-1000XM5')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'Sony WH-1000XM5', N'Over-ear wireless headphones with noise cancellation.', 8490000, N'sony-xm5.jpg', 1, @ElectronicsCatId, '2026-09-05T02:00:00');

        IF NOT EXISTS (SELECT 1 FROM products WHERE ProductName = N'Logitech MX Master 3')
            INSERT INTO products (ProductName, Description, Price, Images, Status, CategoryId, CreatedAt)
            VALUES (N'Logitech MX Master 3', N'Ergonomic wireless mouse for productivity.', 2490000, N'logitech-mx.jpg', 1, @ElectronicsCatId, '2026-09-05T01:00:00');
    END;

    COMMIT TRANSACTION;
    PRINT N'Migration completed successfully.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    DECLARE @errNum INT = ERROR_NUMBER();
    DECLARE @errMsg NVARCHAR(4000) = ERROR_MESSAGE();
    PRINT N'Migration rolled back due to error ' + CAST(@errNum AS NVARCHAR(10)) + N': ' + @errMsg;
    THROW;
END CATCH;
GO