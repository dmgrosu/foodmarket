create table if not exists rama_fm.brand
(
    id         serial                   not null
        constraint brand_pk
            primary key,
    name       text                     not null,
    erp_code   text,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create unique index brand_erp_code_uindex
    on rama_fm.brand (erp_code);

create table if not exists rama_fm.good_group
(
    id              serial                   not null
        constraint good_group_pk
            primary key,
    name            text                     not null,
    parent_group_id integer,
    erp_code        text,
    created_at      timestamp with time zone not null default now(),
    deleted_at      timestamp with time zone,
    updated_at      timestamp with time zone
);

create unique index good_group_erp_code_uindex
    on rama_fm.good_group (erp_code);

create table if not exists rama_fm.good
(
    id         serial                   not null
        constraint good_pk
            primary key,
    name       text                     not null,
    price      numeric                           default 0 not null,
    brand_id   integer
        constraint good_brand_id_fk
            references rama_fm.brand (id),
    group_id   integer
        constraint good_group_id_fk
            references rama_fm.good_group (id),
    unit       text,
    package    numeric,
    erp_code   text,
    bar_code   text,
    weight     numeric,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone,
    updated_at timestamp with time zone
);

create unique index good_erp_code_uindex
    on rama_fm.good (erp_code);
create index good_brand_id_index
    on rama_fm.good (brand_id);
create index good_group_id_index
    on rama_fm.good (group_id);

create table if not exists rama_fm.client
(
    id         serial                   not null
        constraint client_pk
            primary key,
    name       text                     not null,
    idno       char(13)                 not null,
    created_at timestamp with time zone not null default now(),
    deleted_at timestamp with time zone
);

create unique index client_idno_uindex
    on rama_fm.client (idno);

create table if not exists rama_fm."order"
(
    id                serial    not null
        constraint order_pk
            primary key,
    client_id         integer
        constraint order_client_id_fk
            references rama_fm.client (id),
    total_sum         numeric            default 0 not null,
    created_at        timestamp not null default now(),
    deleted_at        timestamp,
    processed_at      timestamp,
    processing_result text,
    status            text      default 'NEW'::text not null
);

create table if not exists rama_fm.order_good
(
    id       serial            not null
        constraint order_good_pk
            primary key,
    order_id integer           not null
        constraint order_good_order_id_fk
            references rama_fm.order (id),
    good_id  integer           not null
        constraint order_good_good_id_fk
            references rama_fm.good (id),
    quantity numeric default 0 not null,
    sum      numeric default 0 not null,
    weight   numeric
);

create table if not exists rama_fm."app_user"
(
    id         serial                   not null
        constraint app_user_pk
            primary key,
    email      text                     not null,
    passwd     text                     not null,
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
