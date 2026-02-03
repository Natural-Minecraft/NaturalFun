# NaturalFun v1.1.0 (Bloodmoon Edition)

Hadirkan keseruan ekstra di server **NaturalSMP** dengan fitur event Bloodmoon, NPC Trader kustom, serta berbagai perintah menyenangkan lainnya.

## ✨ Fitur Utama

### 🌑 Bloodmoon System v2.0
*   **BossBar Timer**: Bar merah dengan countdown real-time (MM:SS) yang muncul otomatis saat event dimulai.
*   **Elite Mobs**: Bos-bos tertentu mendapatkan health dan damage multiplier otomatis selama malam berdarah.
*   **Bloodmoon Shop**: Tukarkan `Bloodmoon Coin` yang didapat dari monster dengan item eksklusif di GUI Shop.
*   **Leaderboard**: Pantau siapa pembasmi monster terbanyak dengan `/bmleaderboard`.
*   **Safezones**: Dukungan area aman (Safezone) di mana monster Bloodmoon tidak bisa menyerang.

### 🏪 Professional NPC Trader
*   **Modern API Integration**: Menggunakan sistem *Modern Adventure* untuk nama NPC yang lebih berwarna dan anti-error.
*   **Custom Merchant**: Buat pedagang warga (Villager) statis dengan inventory dagangan yang bisa sepenuhnya dikustomisasi.
*   **Persistent Data**: Lokasi dan data trader tersimpan aman bahkan setelah server restart.

### 🎉 Fun Modules
*   **Broadcaster Commands**: Command `/gg` dan `/noob` yang unik dengan pesan broadcast keren.
*   **Warden XP Bonus**: Bonus XP khusus bagi pemain yang berhasil menaklukkan Warden.

## 📑 Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/bloodmoon` | `bm` | Main Admin Command / Open Admin GUI | `bloodmoon.admin` |
| `/bmshop` | - | Buka Toko Bloodmoon atau Editor Toko | `bloodmoon.admin` (untuk editor) |
| `/bmleaderboard`| `bmtop` | Lihat Top Kills Bloodmoon | - |
| `/traderadmin` | - | Kelola NPC Trader (Create/Remove/Reload) | `naturalfun.admin` |
| `/givebmcoin` | - | Beri koin Bloodmoon ke player | `bloodmoon.admin` |
| `/gg` | - | Kirim pesan GG ke seluruh server | - |
| `/noob` | - | Kirim pesan kocak noob | - |

## 🛠️ Technical Fixes (v1.1.0)
*   **Package Standardization**: Perbaikan total error "duplicate class" dengan standarisasi package name `id.naturalsmp.naturalFun`.
*   **Modern API Update**: Penggunaan `customName()` (Adventure) menggantikan `setCustomName()` yang sudah usang.
*   **Map Initialization**: Perbaikan issue peta kosong di loot/rewards.

---
**© 2026 NaturalSMP Development Team**
