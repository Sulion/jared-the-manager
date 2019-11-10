create table expenses
(
    ID               INTEGER PRIMARY KEY,
    AUTHORIZED_BY    TEXT,
    AMOUNT           DECIMAL,
    CATEGORY         TEXT,
    TRANSACTION_DATE DATE,
    COMMENT          text
)