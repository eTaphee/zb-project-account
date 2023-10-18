insert into account_user(id, name, created_at, updated_at)
values (1, 'zero', now(), now());

insert into account_user(id, name, created_at, updated_at)
values (2, 'base', now(), now());

insert into account_user(id, name, created_at, updated_at)
values (3, 'backend', now(), now());

insert into account(account_user_id, account_status, account_number, balance)
values (1, 'IN_USE', '1000000000', 10000);

insert into account(account_user_id, account_status, account_number, balance)
values (1, 'IN_USE', '1000000001', 0);

insert into account(account_user_id, account_status, account_number, balance)
values (1, 'UNREGISTERED', '1000000002', 0);

insert into account(account_user_id, account_status, account_number, balance)
values (2, 'IN_USE', '2000000000', 50000);