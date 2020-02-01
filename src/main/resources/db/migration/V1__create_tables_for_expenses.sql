create table expenses
(
    ID               BIGINT PRIMARY KEY,
    ACCOUNT_ID       INTEGER,
    MSG_ID           INTEGER,
    AUTHORIZED_BY    TEXT,
    AMOUNT           DECIMAL,
    CATEGORY         TEXT,
    TRANSACTION_DATE DATE,
    COMMENT          text
)