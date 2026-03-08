create schema if not exists rama_fm;

create table if not exists rama_fm.brand
(
    id         serial                   not null
        constraint brand_pk
            primary key,
    name       text                     not null,
    erp_code   text                     not null,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create unique index if not exists brand_erp_code_uindex
    on rama_fm.brand (erp_code);

create table if not exists rama_fm.product_group
(
    id              bigserial                not null
        constraint product_group_pk
            primary key,
    name            text                     not null,
    parent_group_id integer,
    erp_code        text                     not null,
    created_at      timestamp with time zone not null default now(),
    deleted_at      timestamp with time zone,
    updated_at      timestamp with time zone
);

create unique index if not exists product_group_erp_code_uindex
    on rama_fm.product_group (erp_code);

create table if not exists rama_fm.product
(
    id         bigserial                not null
        constraint product_pk
            primary key,
    name       text                     not null,
    brand_id   integer
        constraint product_brand_id_fk
            references rama_fm.brand (id),
    group_id   integer
        constraint product_group_id_fk
            references rama_fm.product_group (id),
    unit       text,
    package    numeric,
    erp_code   text                     not null,
    bar_code   text,
    weight     numeric,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone,
    updated_at timestamp with time zone
);

create unique index if not exists product_erp_code_uindex
    on rama_fm.product (erp_code);
create index if not exists product_brand_id_index
    on rama_fm.product (brand_id);
create index if not exists product_group_id_index
    on rama_fm.product (group_id);

create table if not exists rama_fm.client
(
    id         bigserial                not null
        constraint client_pk
            primary key,
    name       text                     not null,
    idno       char(13)                 not null,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create unique index if not exists client_idno_uindex
    on rama_fm.client (idno);

create table if not exists rama_fm."order"
(
    id                bigserial         not null
        constraint order_pk
            primary key,
    client_id         integer
        constraint order_client_id_fk
            references rama_fm.client (id),
    total_sum         numeric default 0 not null,
    created_at        timestamp         not null default now(),
    deleted_at        timestamp,
    processed_at      timestamp,
    processing_result text,
    status            text    default 'NEW'::text not null
);

create table if not exists rama_fm.order_product
(
    id         serial            not null
        constraint order_product_pk
            primary key,
    order_id   integer           not null
        constraint order_product_order_id_fk
            references rama_fm.order (id),
    product_id integer           not null
        constraint order_product_product_id_fk
            references rama_fm.product (id),
    quantity   numeric default 0 not null,
    price      numeric default 0 not null,
    sum        numeric default 0 not null,
    weight     numeric
);

create table if not exists rama_fm."app_user"
(
    id         serial                   not null
        constraint app_user_pk
            primary key,
    email      text                     not null,
    passwd     text                     not null,
    state      text                     not null,
    client_id  integer
        constraint app_user_client_id_fk
            references rama_fm.client (id),
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create table if not exists rama_fm."app_user_role"
(
    id         serial                   not null
        constraint app_user_role_pk
            primary key,
    "user_id"  integer                  not null
        constraint app_role_app_user_id_fk
            references rama_fm.app_user (id),
    role       text                     not null,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create table if not exists rama_fm."storages"
(
    id       serial
        constraint storages_pk
            primary key,
    name     text not null,
    erp_code text not null
);

create table if not exists rama_fm."prices"
(
    id         bigserial
        constraint prices_pk
            primary key,
    type       text not null,
    product_id bigserial
        constraint prices_product_id_fk
            references rama_fm.product
                on delete cascade,
    storage_id serial
        constraint prices_storages_id_fk
            references rama_fm.storages
);

create index if not exists prices_type_storage_id_product_id_index
    on rama_fm.prices (type, storage_id, product_id);

create table if not exists rama_fm.balances
(
    id         bigserial
        constraint balances_pk
            primary key,
    storage_id serial
        constraint balances_storages_id_fk
            references rama_fm.storages
                on delete cascade,
    product_id bigserial
        constraint balances_product_id_fk
            references rama_fm.product
                on delete cascade,
    quantity   numeric not null
);

create unique index if not exists balances_product_id_storage_id_uindex
    on rama_fm.balances (product_id, storage_id);
