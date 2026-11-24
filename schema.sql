CREATE TABLE IF NOT EXISTS customer (
    customer_id    VARCHAR(10)  PRIMARY KEY,
    first_name     VARCHAR(50)  NOT NULL,
    surname        VARCHAR(50)  NOT NULL,
    address        VARCHAR(100),
    phone          VARCHAR(20),
    username       VARCHAR(30)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS account (
    account_number   VARCHAR(15)   PRIMARY KEY,
    customer_id      VARCHAR(10)   NOT NULL,
    account_type     VARCHAR(15)   NOT NULL,
    branch           VARCHAR(50),
    date_opened      DATE          NOT NULL,
    balance          DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    employer_name    VARCHAR(80),
    employer_address VARCHAR(100),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_number   VARCHAR(15)   NOT NULL,
    type             VARCHAR(15)   NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    transaction_date DATETIME      NOT NULL,
    balance_after    DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (account_number) REFERENCES account(account_number)
);
