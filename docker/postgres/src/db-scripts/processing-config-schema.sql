create schema if not exists gms_config;

comment on schema gms_config is 'GMS Processing Configuration Schema';

set search_path to gms_config;

create type operator_type_enum as enum (
  'IN',
  'EQ'
);

create type phase_type_enum as enum (
	'UNKNOWN',
	'P',
	'S',
	'I',
	'l',
	'Lg',
	'LQ',
	'LR',
	'nNL',
	'NP',
	'NP_1',
	'P3KP',
	'P3KPbc',
	'P3KPbc_B',
	'P3KPdf',
	'P3KPdf_B',
	'P4KP',
	'P4KPbc',
	'P4KPdf',
	'P4KPdf_B',
	'P5KP',
	'P5KPbc',
	'P5KPbc_B',
	'P5KPdf',
	'P5KPdf_B',
	'P5KPdf_C',
	'P7KP',
	'P7KPbc',
	'P7KPbc_B',
	'P7KPbc_C',
	'P7KPdf',
	'P7KPdf_B',
	'P7KPdf_C',
	'P7KPdf_D',
	'Pb',
	'PcP',
	'PcS',
	'Pdiff',
	'Pg',
	'PKhKP',
	'PKiKP',
	'PKKP',
	'PKKPab',
	'PKKPbc',
	'PKKPdf',
	'PKKS',
	'PKKSab',
	'PKKSbc',
	'PKKSdf',
	'PKP',
	'PKP2',
	'PKP2ab',
	'PKP2bc',
	'PKP2df',
	'PKP3',
	'PKP3ab',
	'PKP3bc',
	'PKP3df',
	'PKP3df_B',
	'PKPab',
	'PKPbc',
	'PKPdf',
	'PKPPKP',
	'PKS',
	'PKSab',
	'PKSbc',
	'PKSdf',
	'PmP',
	'Pn',
	'PnPn',
	'PP',
	'pP',
	'PP_1',
	'PP_B',
	'pPdiff',
	'pPKiKP',
	'pPKP',
	'pPKPab',
	'pPKPbc',
	'pPKPdf',
	'PPP',
	'PPP_B',
	'PPS',
	'PPS_B',
	'PS',
	'PS_1',
	'pSdiff',
	'pSKS',
	'pSKSac',
	'pSKSdf',
	'Rg',
	'Sb',
	'ScP',
	'ScS',
	'Sdiff',
	'Sg',
	'SKiKP',
	'SKKP',
	'SKKPab',
	'SKKPbc',
	'SKKPdf',
	'SKKS',
	'SKKSac',
	'SKKSac_B',
	'SKKSdf',
	'SKP',
	'SKPab',
	'SKPbc',
	'SKPdf',
	'SKS',
	'SKS2',
	'SKS2ac',
	'SKS2df',
	'SKSac',
	'SKSdf',
	'SKSSKS',
	'Sn',
	'SnSn',
	'SP',
	'SP_1',
	'sPdiff',
	'sPKiKP',
	'sPKP',
	'sPKPab',
	'sPKPbc',
	'sPKPdf',
	'SS',
	'SS_1',
	'SS_B',
	'sSdiff',
	'sSKS',
	'sSKSac',
	'sSKSdf',
	'SSS',
	'SSS_B',
	'T'
);

create table if not exists configuration(
    id serial primary key,
    name varchar(100) not null unique
);

create table if not exists configuration_option(
  id serial primary key,
  name varchar(100) not null,
	parameters jsonb not null,
	configuration_id integer constraint configuration_option_configuration_id_fkey references configuration(id) not null
);

create table if not exists configuration_constraint(
  id serial primary key,
	constraint_type varchar(100) not null,
  criterion varchar(100) not null,
	priority bigint not null,
	operator_type operator_type_enum not null,
	negated boolean not null,
	configuration_option_id integer constraint configuration_constraint_configuration_option_id_fkey references configuration_option(id) not null,
	boolean_value boolean,
	numeric_scalar_value double precision,
	numeric_range_min_value double precision,
	numeric_range_max_value double precision,
	time_of_day_range_min_value time,
	time_of_day_range_max_value time,
	time_of_year_range_min_value timestamp,
	time_of_year_range_max_value timestamp
);

create table if not exists string_constraint_value(
  constraint_id integer references configuration_constraint(id) not null,
  value varchar(100) not null
);

create table if not exists phase_constraint_value(
  constraint_id integer references configuration_constraint(id) not null,
  value phase_type_enum not null
);

create sequence if not exists configuration_sequence owned by configuration.id;
create sequence if not exists configuration_option_sequence owned by configuration_option.id;
create sequence if not exists constraint_sequence owned by configuration_constraint.id;

-- set gms_admin user for system to use with database
revoke all on schema gms_config from gms_admin;
grant usage on schema gms_config to gms_admin;
grant usage on sequence configuration_sequence to gms_admin;
grant usage on sequence configuration_option_sequence to gms_admin;
grant usage on sequence constraint_sequence to gms_admin;
grant select, insert, update, delete, truncate on all tables in schema gms_config to gms_admin;

-- set up gms_config_application user for hibernate to use to connect to the config database
create role gms_config_application with noinherit login encrypted password 'GMS_POSTGRES_CONFIG_APPLICATION_PASSWORD';

revoke all on schema gms_config from gms_config_application;
grant usage on schema gms_config to gms_config_application;
grant usage on sequence configuration_sequence to gms_config_application;
grant usage on sequence configuration_option_sequence to gms_config_application;
grant usage on sequence constraint_sequence to gms_config_application;
grant select, insert, update, delete, truncate on all tables in schema gms_config to gms_config_application;

-- set up gms_read_only user for developers to use to connect to the database
revoke all on schema gms_config from gms_read_only;
grant usage on schema gms_config to gms_read_only;
grant select on all tables in schema gms_config to gms_read_only;


alter table configuration owner to gms_admin;
alter table configuration_option owner to gms_admin;
alter table configuration_constraint owner to gms_admin;
alter table string_constraint_value owner to gms_admin;
alter table phase_constraint_value owner to gms_admin;
alter sequence configuration_sequence owner to gms_admin;
alter sequence configuration_option_sequence owner to gms_admin;
alter sequence constraint_sequence owner to gms_admin;
alter schema gms_config owner to gms_admin;

--TODO -tpf - 1/23/20 - from MR: Not critical immediately, but gms_admin should be able to modify the schema, but no drop table/schema.
