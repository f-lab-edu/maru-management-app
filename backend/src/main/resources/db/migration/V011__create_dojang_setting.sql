CREATE TABLE dojang_setting (
    id VARCHAR(13) PRIMARY KEY,
    dojang_id VARCHAR(13) NOT NULL UNIQUE,
    default_tuition INT NULL,
    auto_invoice_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_invoice_day INT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (dojang_id) REFERENCES dojang(id)
);
