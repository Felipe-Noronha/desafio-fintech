CREATE TABLE tb_user (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE tb_account (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
);

CREATE TABLE tb_transaction (
    id UUID PRIMARY KEY,
    payer_account_id BIGINT NOT NULL,
    payee_account_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_transaction_payer FOREIGN KEY (payer_account_id) REFERENCES tb_account(id),
    CONSTRAINT fk_transaction_payee FOREIGN KEY (payee_account_id) REFERENCES tb_account(id)
);


