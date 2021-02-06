create table if not exists rama_fm.brand
(
    id serial not null
    constraint brand_pk
    primary key,
    name text not null
);

create table if not exists rama_fm.good
(
    id serial not null
    constraint good_pk
    primary key,
    name text not null,
    price numeric default 0 not null,
    brand_id integer
    constraint good_brand_id_fk
    references rama_fm.brand
);

create table if not exists rama_fm.client
(
    id serial not null
    constraint client_pk
    primary key,
    name text not null,
    idno char(13) not null
    );

create table if not exists rama_fm.good_group
(
    id serial not null
    constraint good_group_pk
    primary key,
    name text not null,
    parent_group_id integer
);

create table if not exists rama_fm."order"
(
    id serial not null
    constraint order_pk
    primary key,
    order_date timestamp with time zone not null,
    client_id integer
    constraint order_client_id_fk
    references rama_fm.client,
    total_sum numeric default 0 not null
);

create table if not exists rama_fm.order_good
(
    id serial not null
    constraint order_good_pk
    primary key,
    order_id integer not null,
    good_id integer not null,
    quantity numeric default 0 not null,
    sum numeric default 0 not null
);

create type app_role as enum ('admin', 'user');
