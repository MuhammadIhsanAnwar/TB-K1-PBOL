-- =====================================================================
-- account_crud.sql
-- Jalankan SETELAH online_schema.sql, di Supabase Dashboard -> SQL Editor.
-- Aman dijalankan ulang (create or replace / revoke / grant bersifat idempoten).
--
-- Tujuan:
--   1. Memperbaiki trigger agar nama yang diubah user tidak ditimpa ulang
--      dari metadata auth setiap kali user login.
--   2. Menyediakan operasi Update/Delete akun lewat fungsi RPC
--      (security definer) tanpa membuka hak UPDATE/DELETE langsung pada
--      tabel public.users. Tabel tetap read-only bagi klien.
-- =====================================================================

-- 1. Trigger: pertahankan nama yang sudah ada, isi dari metadata hanya jika kosong.
--    Tanpa ini, setiap login (auth.users di-update) akan menimpa nama hasil edit.
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
        nama = case when existing_user.nama = '' then excluded.nama
                    else existing_user.nama end,
        last_active_at = coalesce(excluded.last_active_at, existing_user.last_active_at),
        verified_at = excluded.verified_at;
    return new;
end;
$$;

-- 2. UPDATE: user mengubah nama miliknya sendiri.
create or replace function public.update_own_profile(p_nama text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception 'Tidak terautentikasi.';
    end if;
    if p_nama is null or btrim(p_nama) = '' then
        raise exception 'Nama tidak boleh kosong.';
    end if;
    update public.users
    set nama = btrim(p_nama)
    where id = auth.uid() and is_active;
    if not found then
        raise exception 'Profil tidak ditemukan atau akun nonaktif.';
    end if;
end;
$$;

-- 3. UPDATE (admin): aktifkan / nonaktifkan akun UMKM.
create or replace function public.admin_set_user_active(p_user_id uuid, p_active boolean)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    if not public.is_super_admin() then
        raise exception 'Hanya Super Admin yang dapat mengubah status akun.';
    end if;
    if p_user_id = auth.uid() then
        raise exception 'Super Admin tidak dapat mengubah status akunnya sendiri.';
    end if;
    update public.users
    set is_active = p_active
    where id = p_user_id and role <> 'super_admin';
    if not found then
        raise exception 'Pengguna tidak ditemukan atau berstatus Super Admin.';
    end if;
end;
$$;

-- 4. DELETE: user menghapus akunnya sendiri.
--    Menghapus baris di auth.users; public.users dan umkm_workspaces ikut
--    terhapus lewat ON DELETE CASCADE.
create or replace function public.delete_own_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception 'Tidak terautentikasi.';
    end if;
    if public.is_super_admin() then
        raise exception 'Akun Super Admin tidak dapat dihapus lewat aplikasi.';
    end if;
    delete from auth.users where id = auth.uid();
end;
$$;

-- 5. DELETE (admin): hapus akun UMKM lain.
create or replace function public.admin_delete_user(p_user_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    if not public.is_super_admin() then
        raise exception 'Hanya Super Admin yang dapat menghapus akun.';
    end if;
    if p_user_id = auth.uid() then
        raise exception 'Super Admin tidak dapat menghapus akunnya sendiri.';
    end if;
    if exists (select 1 from public.users where id = p_user_id and role = 'super_admin') then
        raise exception 'Akun Super Admin tidak dapat dihapus lewat aplikasi.';
    end if;
    delete from auth.users where id = p_user_id;
    if not found then
        raise exception 'Pengguna tidak ditemukan.';
    end if;
end;
$$;

-- 6. Hak eksekusi: hanya user yang sudah login. Pemeriksaan role ada di dalam fungsi.
revoke all on function public.update_own_profile(text) from public, anon;
revoke all on function public.admin_set_user_active(uuid, boolean) from public, anon;
revoke all on function public.delete_own_account() from public, anon;
revoke all on function public.admin_delete_user(uuid) from public, anon;
grant execute on function public.update_own_profile(text) to authenticated;
grant execute on function public.admin_set_user_active(uuid, boolean) to authenticated;
grant execute on function public.delete_own_account() to authenticated;
grant execute on function public.admin_delete_user(uuid) to authenticated;

-- 7. Muat ulang schema cache PostgREST agar fungsi langsung bisa dipanggil.
notify pgrst, 'reload schema';
