insert into account_user(id, name, created_at, updated_at)
values (1, 'pororo', now(), now());

insert into account_user(id, name, created_at, updated_at)
values (2, 'lupi', now(), now());

insert into account_user(id, name, created_at, updated_at)
values (3, 'eddie', now(), now());

insert into account (id, account_user_id, account_number, account_status, balance, registered_at)
values (1, 1, '1000000000', 'IN_USE', 100000, now());