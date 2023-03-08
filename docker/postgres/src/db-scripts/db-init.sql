select 'create database gms' where not exists (select from pg_database where datname ='gms')\gexec

create role gms_admin with noinherit login encrypted password 'GMS_POSTGRES_ADMIN_PASSWORD';

create role gms_read_only with noinherit login encrypted password 'GMS_POSTGRES_READ_ONLY_PASSWORD';
