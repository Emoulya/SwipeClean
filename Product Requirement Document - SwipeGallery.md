# Product Requirement Document (PRD)

## Project: SwipeGallery (Aplikasi Galeri & Pembersih Media Berbasis Swipe)

### 1\. Ringkasan Eksekutif (Executive Summary)

**SwipeGallery** adalah aplikasi galeri media Android modern yang dirancang untuk mempermudah dan mempercepat proses kurasi serta pembersihan penyimpanan internal HP. Berbeda dengan aplikasi galeri konvensional yang menuntut seleksi manual foto satu per satu, SwipeGallery mengadopsi mekanisme gestur interaktif (_swipe deck_ ala Tinder): **geser kiri untuk menghapus** (pindah ke sampah) dan **geser kanan untuk menyimpan**.

Fokus utama produk adalah efisiensi waktu, fluiditas animasi 60–120 FPS, dan integrasi mulus dengan sistem keamanan media Android (_Scoped Storage_).

### 2\. Latar Belakang & Masalah Pengguna (Problem Statement)

#### 2.1 Profil Pengguna (Target Persona)

- **Karakteristik:** Pemilik smartphone dengan ratusan hingga ribuan foto/video menumpuk (tangkapan layar, foto buram, foto duplikat, dokumen sekali pakai, media WhatsApp/sosmed).
- **Pain Point:**
  1. Memori internal sering penuh, memicu notifikasi peringatan sistem.
  2. Proses sortir galeri bawaan sangat lambat dan melelahkan (harus tap thumbnail kecil, masuk full screen, tekan tombol hapus, konfirmasi, lalu kembali lagi).
  3. Menunda pembersihan file hingga memori benar-benar habis.

#### 2.2 Solusi Produk

Aplikasi galeri yang mengubah pekerjaan bersih-bersih membosankan menjadi aktivitas cepat dan memuaskan (_gamified utility_) dengan gestur satu tangan.

### 3\. Value Proposition & Key Differentiator

- **Kecepatan Sortir:** Meninjau dan memilah puluhan media dalam hitungan detik.
- **Aman dari Salah Hapus:** Menggunakan sistem penampungan sampah (_recycle bin_) bertenggang waktu (7, 14, 30 hari) serta dialog verifikasi resmi Android OS.
- **Hemat Waktu & Anti-Bosan:** Transisi gestur intuitif dengan _haptic feedback_.

### 4\. User Journey & Onboarding ("Aha! Moment")

1. **First Launch (Kunjungan Pertama):**
   - Pengguna membuka aplikasi -> Aplikasi meminta izin akses media (READ_MEDIA_IMAGES / READ_MEDIA_VIDEO).
   - Setelah izin diberikan, galeri foto langsung termuat instan dalam grid rapi tanpa tutorial bertele-tele.
2. **The "Aha! Moment":**
   - Pengguna mengetuk salah satu foto -> Masuk ke mode kartu layar penuh.
   - Muncul petunjuk visual halus (indikator swipe kiri/kanan).
   - Pengguna menggeser kartu ke kiri -> Foto melayang keluar dan masuk antrean sampah -> Pengguna langsung merasakan efisiensi dan kecepatan interaksi.

### 5. Rincian Fitur & Prioritas (Feature Specifications)

Fitur diklasifikasikan menggunakan metode **MoSCoW** (*Must Have, Should Have, Could Have*).

| Prioritas            | Modul / Fitur                     | Deskripsi Fungsional                                                                                                                                                                              |
| -------------------- | --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **P0 (Must Have)**   | **Grid Gallery View**             | Menampilkan seluruh foto dan video dari perangkat dalam tata letak grid responsif, dengan pengelompokan berdasarkan tanggal/bulan.                                                                |
| **P0 (Must Have)**   | **Full Screen Card Review**       | Menampilkan media satu per satu dalam tampilan layar penuh saat pengguna mengetuk media dari *Grid View*.                                                                                         |
| **P0 (Must Have)**   | **Swipe Gestures**                | **Geser kanan:** menyimpan media dan melanjutkan ke kartu berikutnya.<br>**Geser kiri:** menandai media untuk dipindahkan ke folder Sampah.                                                       |
| **P0 (Must Have)**   | **Batch Trash Execution**         | Mengintegrasikan `MediaStore.createTrashRequest` pada Android 11+ agar pengguna dapat menyetujui pemindahan file ke Sampah secara kolektif tanpa menampilkan dialog konfirmasi untuk setiap foto. |
| **P1 (Should Have)** | **Folder Sampah (Trash Bin)**     | Menyediakan halaman khusus untuk melihat daftar item yang ditandai untuk dihapus, lengkap dengan opsi **Restore** (pulihkan) dan **Empty Trash Now** (hapus permanen).                            |
| **P1 (Should Have)** | **Video Auto-Play & Mute Switch** | Video otomatis diputar saat kartu video muncul di layar. Sakelar *mute* aktif secara default agar audio video tidak mengagetkan pengguna.                                                         |
| **P1 (Should Have)** | **Pengaturan Siklus Sampah**      | Menyediakan opsi interval retensi Sampah: penghapusan permanen otomatis setelah **7 hari, 14 hari, atau 30 hari**, dengan tetap mengikuti kebijakan sistem operasi.                               |
| **P2 (Could Have)**  | **Haptic Feedback & Filter**      | Memberikan getaran ringan (*haptic feedback*) saat kartu dilempar (*swipe*), serta menyediakan filter untuk menampilkan hanya **Screenshots**, **Videos**, atau **Large Files**.                  |


### 6\. Alur Kerja Navigasi (Screen Flow)

\[Splash / Permission Check\]  
│  
▼  
\[Home: Grid Gallery\] ──(Pilih Tab)──► \[Trash Bin Screen\]  
│ │  
(Ketuk Foto) (Restore / Empty)  
│  
▼  
\[Swipe Review Mode (Deck)\]  
├── Swipe Kanan ──► Tetap Simpan ──► Next Card  
└── Swipe Kiri ──► Masuk Antrean Hapus ──► Next Card  
│  
(Selesai / Keluar)  
│  
▼  
\[Dialog Konfirmasi Sistem OS (MediaStore Trash)\]  
<br/>