-- 사용자 1
insert into account_user(id, name, created_at, updated_at)
values (1, 'zero', now(), now());

-- 사용자 2
insert into account_user(id, name, created_at, updated_at)
values (2, 'base', now(), now());

-- 사용자 3
insert into account_user(id, name, created_at, updated_at)
values (3, 'backend', now(), now());

-- 사용자 1, 계좌(정상)
insert into account(account_user_id, account_status, account_number, balance)
values (1, 'IN_USE', '1000000000', 10000);

-- 사용자 1, 계좌(정상, 잔고 없음)
insert into account(account_user_id, account_status, account_number, balance)
values (1, 'IN_USE', '1000000001', 0);

-- 사용자 1, 계좌(해지)
insert into account(account_user_id, account_status, account_number, balance)
values (1, 'UNREGISTERED', '1000000002', 0);

-- 사용자 2, 계좌(정상)
insert into account(account_user_id, account_status, account_number, balance)
values (2, 'IN_USE', '2000000000', 50000);

-- 잔액 사용 성공, 사용자 1 계좌 1
insert into transaction(id, transaction_type, transaction_result_Type, error_code, account_id, amount, balance_snapshot, transaction_id, transacted_at, transaction_for_cancel_id, is_canceled)
values (1, 'USE', 'S', null, 1, 1000, 9000, 'TRAN1', now(), null, false);

-- 잔액 사용 실패, 사용자 1 계좌 1
insert into transaction(id, transaction_type, transaction_result_Type, error_code, account_id, amount, balance_snapshot, transaction_id, transacted_at, transaction_for_cancel_id, is_canceled)
values (2, 'USE', 'F', 'INVALID_REQUEST', 1, 1000, 9000, 'TRAN2', now(), null, false);

-- 1번 거래 취소 내역, 사용자 1 계좌 1
insert into transaction(id, transaction_type, transaction_result_Type, error_code, account_id, amount, balance_snapshot, transaction_id, transacted_at, transaction_for_cancel_id, is_canceled)
values (3, 'CANCEL', 'S', null, 1, 1000, 9000, 'TRAN3', now(), null, false);

-- 취소된 거래 내역, 사용자 1 계좌 1
insert into transaction(id, transaction_type, transaction_result_Type, error_code, account_id, amount, balance_snapshot, transaction_id, transacted_at, transaction_for_cancel_id, is_canceled)
values (4, 'USE', 'S', null, 1, 1000, 9000, 'TRAN4', now(), null, true);

-- 잔액 사용 성공, 사용자 2 계좌 1
insert into transaction(id, transaction_type, transaction_result_Type, error_code, account_id, amount, balance_snapshot, transaction_id, transacted_at, transaction_for_cancel_id, is_canceled)
values (5, 'USE', 'S', null, 4, 1000, 9000, 'TRAN5', now(), null, false);

-- transaction 테이블 id 재설정
ALTER TABLE transaction ALTER COLUMN id RESTART WITH 6;