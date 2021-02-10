create table if not exists "brand"
(
    id         int       not null
        constraint brand_pk
            primary key auto_increment,
    name       text      not null,
    "erp_code"   text,
    "created_at" timestamp not null default now(),
    "deleted_at" timestamp
);

create table if not exists "good_group"
(
    id              int       not null
        constraint good_group_pk
            primary key auto_increment,
    name            text      not null,
    parent_group_id integer,
    erp_code        text,
    "created_at"      timestamp not null default now(),
    "deleted_at"      timestamp,
    "updated_at"      timestamp
);

create table if not exists "good"
(
    id         int       not null
        constraint good_pk
            primary key auto_increment,
    name       text      not null,
    price      numeric            default 0 not null,
    "brand_id"   integer
        constraint good_brand_id_fk
            references "brand",
    "group_id"   integer
        constraint good_group_id_fk
            references "good_group" (id),
    unit       text,
    "package"    numeric,
    "erp_code"   text,
    "bar_code"   text,
    weight     numeric,
    "created_at" timestamp not null default now(),
    "deleted_at" timestamp,
    "updated_at" timestamp
);

create table if not exists "client"
(
    id           int       not null
        constraint client_pk
            primary key auto_increment,
    name         text      not null,
    idno         char(13)  not null,
    "created_at" timestamp not null default now(),
    "deleted_at" timestamp
);

create table if not exists "order"
(
    id                int       not null
        constraint order_pk
            primary key auto_increment,
    client_id         integer
        constraint order_client_id_fk
            references "client",
    total_sum         numeric            default 0 not null,
    created_at        timestamp not null default now(),
    deleted_at        timestamp,
    processed_at      timestamp,
    processing_result text
);

create table if not exists "order_good"
(
    id       int               not null
        constraint order_good_pk
            primary key auto_increment,
    order_id integer           not null,
    good_id  integer           not null,
    quantity numeric default 0 not null,
    sum      numeric default 0 not null
);

create table if not exists "app_user"
(
    id           int       not null
        constraint app_user_pk
            primary key auto_increment,
    email        text      not null,
    passwd       text      not null,
    "client_id"  integer
        constraint app_user_client_id_fk
            references "client",
    "created_at" timestamp not null default now(),
    "deleted_at" timestamp
);

create table if not exists "app_user_role"
(
    id         int       not null
        constraint app_user_role_pk
            primary key auto_increment,
    "user_id"  integer   not null,
    role       text      not null,
    created_at timestamp not null default now(),
    deleted_at timestamp
);
