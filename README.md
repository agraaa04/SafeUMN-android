# SafeUMN — Aplikasi Darurat Kampus UMN

SafeUMN adalah aplikasi Android untuk **tombol darurat (SOS)**, **pelaporan insiden**, dan **informasi keselamatan** di lingkungan kampus Universitas Multimedia Nusantara. Aplikasi menghubungkan mahasiswa dengan tim Security dan Building Management secara cepat dan terlacak.

> Proyek mata kuliah **IF570 Mobile Application Programming** — Kelompok 7, Informatika UMN (Semester Ganjil 2026/2027).

## Fitur

- **Login dengan Supabase Auth** (email + password) — setiap akun hanya melihat datanya sendiri
- **Tombol SOS** (tekan & tahan 3 detik) — mengirim koordinat GPS asli ke database
- **Buat laporan 4 langkah**: kategori → lokasi (GPS) → deskripsi & foto bukti → ringkasan
- **Foto bukti** dari **kamera (CameraX)** atau **galeri (Photo Picker)**, diunggah ke Supabase Storage
- **Status & riwayat laporan** dengan pencarian, filter waktu, dan detail laporan beserta foto
- **Inbox & alert** keselamatan kampus
- **Profil & pengaturan notifikasi** tersimpan per akun

## Tech Stack

| Bagian | Teknologi |
|---|---|
| Bahasa & UI | Kotlin, Jetpack Compose, Material 3 |
| Arsitektur | MVVM + Repository (UI → ViewModel → Repository → Supabase) |
| Navigasi | Navigation Compose |
| Backend | Supabase (PostgreSQL, Auth, Storage, Row Level Security) |
| Library Supabase | supabase-kt (postgrest, auth, storage) + Ktor |
| Lokasi | Google Play Services — Fused Location Provider |
| Kamera | CameraX + ExifInterface |
| Gambar | Coil |

## Keamanan Data

- **Row Level Security (RLS)**: policy berbasis `auth.uid()` sehingga laporan, SOS, dan profil hanya bisa diakses pemiliknya.
- Aplikasi hanya boleh membuat laporan berstatus `DIPROSES`; perubahan status dilakukan petugas.
- URL & key Supabase disimpan di `local.properties` dan dibaca lewat `BuildConfig` (tidak ikut ter-commit).

## Menjalankan Project

1. Clone repository ini dan buka dengan Android Studio.
2. Siapkan project Supabase (tabel, storage bucket `report-evidence`, dan policy RLS).
3. Tambahkan ke `local.properties`:
   ```properties
   SUPABASE_URL=https://<project-id>.supabase.co
   SUPABASE_KEY=<publishable-key>
   # opsional, untuk tombol SSO demo
   SSO_EMAIL=<email-akun-demo>
   SSO_PASSWORD=<password-akun-demo>
   ```
4. **Sync Gradle**, lalu jalankan di emulator/HP (minSdk 27).

## Screenshot

<!-- Tambahkan screenshot aplikasi di folder docs/ lalu tampilkan di sini, contoh: -->
<!-- ![Home](docs/home.png) -->

## Tim

Kelompok 7 — Informatika, Universitas Multimedia Nusantara:
Raditya Agra, Lucas Stevanus, Muhammad Rassya, Fabrizzio Ananda.
