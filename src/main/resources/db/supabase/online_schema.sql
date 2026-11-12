do $$
begin
    if to_regclass('public.users') is null and to_regclass('public.profiles') is not null then
        alter table public.profiles rename to users;
    end if;
end;
$$;

create table if not exists public.users (
    id uuid primary key references auth.users (id) on delete cascade,
    email text not null,
    nama text not null default '',
    role text not null default 'umkm' check (role in ('super_admin', 'umkm')),
    is_active boolean not null default true,
    last_active_at timestamptz,
    created_at timestamptz not null default now(),
    verified_at timestamptz
);

do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'users'
          and column_name = 'full_name'
    ) and not exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'users'
          and column_name = 'nama'
    ) then
        alter table public.users rename column full_name to nama;
    end if;
end;
$$;

alter table public.users add column if not exists nama text not null default '';
alter table public.users add column if not exists role text not null default 'umkm';
alter table public.users add column if not exists is_active boolean not null default true;
alter table public.users add column if not exists last_active_at timestamptz;
alter table public.users add column if not exists created_at timestamptz not null default now();
alter table public.users add column if not exists verified_at timestamptz;
alter table public.users drop constraint if exists profiles_role_check;
alter table public.users drop constraint if exists users_role_check;
alter table public.users add constraint users_role_check
    check (role in ('super_admin', 'umkm'));

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.users as existing_user (
        id, email, nama, role, last_active_at, created_at, verified_at
    )
    values (
        new.id,
        coalesce(new.email, ''),
        coalesce(new.raw_user_meta_data ->> 'nama',
                 new.raw_user_meta_data ->> 'full_name', ''),
        'umkm',
        new.last_sign_in_at,
        coalesce(new.created_at, now()),
        new.email_confirmed_at
    )
    on conflict (id) do update set
        email = excluded.email,
        nama = excluded.nama,
        last_active_at = coalesce(excluded.last_active_at, existing_user.last_active_at),
        verified_at = excluded.verified_at;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

drop trigger if exists on_auth_user_changed on auth.users;
create trigger on_auth_user_changed
    after update on auth.users
    for each row execute function public.handle_new_user();

insert into public.users as existing_user (
    id, email, nama, last_active_at, created_at, verified_at
)
select
    id,
    coalesce(email, ''),
    coalesce(raw_user_meta_data ->> 'nama',
             raw_user_meta_data ->> 'full_name', ''),
    last_sign_in_at,
    coalesce(created_at, now()),
    email_confirmed_at
from auth.users
on conflict (id) do update set
    email = excluded.email,
    nama = excluded.nama,
    last_active_at = coalesce(excluded.last_active_at, existing_user.last_active_at),
    verified_at = excluded.verified_at;

do $$
declare
    admin_user_id uuid;
begin
    select id into admin_user_id
    from auth.users
    where lower(email) = lower('warungaju@neoverse.my.id')
      and email_confirmed_at is not null;

    if admin_user_id is null then
        raise notice 'Create and verify warungaju@neoverse.my.id in Supabase Auth, then rerun the Super Admin promotion block.';
    else
        update public.users
        set role = 'super_admin', is_active = true
        where id = admin_user_id;
    end if;
end;
$$;

create or replace function public.is_super_admin()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
    from public.users
        where id = (select auth.uid())
          and role = 'super_admin'
          and is_active
    );
$$;

create or replace function public.is_active_user()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
    from public.users
        where id = (select auth.uid())
          and is_active
    );
$$;

revoke all on function public.is_super_admin() from public;
revoke all on function public.is_active_user() from public;
grant execute on function public.is_super_admin() to authenticated;
grant execute on function public.is_active_user() to authenticated;

alter table public.users enable row level security;
drop policy if exists "profiles_read_own_or_admin" on public.users;
drop policy if exists "users_read_own_or_admin" on public.users;
create policy "users_read_own_or_admin"
    on public.users for select to authenticated
    using (id = (select auth.uid()) or (select public.is_super_admin()));
grant select on public.users to authenticated;
comment on table public.users is
    'Data profil aplikasi. Password dikelola Supabase Auth dan tidak disimpan di tabel ini.';

create table if not exists public.umkm_workspaces (
    user_id uuid primary key references auth.users (id) on delete cascade,
    payload jsonb not null default '{}'::jsonb,
    updated_at timestamptz not null default now()
);

create or replace function public.set_workspace_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists set_umkm_workspace_updated_at on public.umkm_workspaces;
create trigger set_umkm_workspace_updated_at
    before update on public.umkm_workspaces
    for each row execute function public.set_workspace_updated_at();

alter table public.umkm_workspaces enable row level security;
drop policy if exists "umkm_workspaces_read_own" on public.umkm_workspaces;
create policy "umkm_workspaces_read_own"
    on public.umkm_workspaces for select to authenticated
    using (user_id = (select auth.uid()) and (select public.is_active_user()));
drop policy if exists "umkm_workspaces_insert_own" on public.umkm_workspaces;
create policy "umkm_workspaces_insert_own"
    on public.umkm_workspaces for insert to authenticated
    with check (user_id = (select auth.uid()) and (select public.is_active_user()));
drop policy if exists "umkm_workspaces_update_own" on public.umkm_workspaces;
create policy "umkm_workspaces_update_own"
    on public.umkm_workspaces for update to authenticated
    using (user_id = (select auth.uid()) and (select public.is_active_user()))
    with check (user_id = (select auth.uid()) and (select public.is_active_user()));
grant select, insert, update on public.umkm_workspaces to authenticated;
