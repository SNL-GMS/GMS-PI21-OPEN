create schema if not exists gms_session;

comment on schema gms_session is 'GMS User Session Store';

set search_path to gms_session;

create table session(
       sid varchar(255) not null collate "default"
       	   constraint session_pkey
	   	      primary key not deferrable,
	sess json not null,
	expire timestamp(6) not null
);

create index IDX_session_expire on session(expire);

revoke all on schema gms_session from gms_admin;
grant usage on schema gms_session to gms_admin;
grant select, insert, update, delete, truncate, references on all tables in schema gms_session to gms_admin;

create role gms_session_application with noinherit login encrypted password 'GMS_POSTGRES_SESSION_APPLICATION_PASSWORD';

revoke all on schema gms_session from gms_session_application;
grant usage on schema gms_session to gms_session_application;
grant select, insert, update, delete on all tables in schema gms_session to gms_session_application;

revoke all on schema gms_session from gms_read_only;
grant usage on schema gms_session to gms_read_only;
grant select on all tables in schema gms_session to gms_read_only;

alter table session owner to gms_admin;
alter schema gms_session owner to gms_admin;
