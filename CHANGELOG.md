# Changelog - NaturalFun 🎈

Dokumentasi riwayat pembaruan, perbaikan bug, dan rilis fitur untuk plugin **NaturalFun** (Fun & Minigames Plugin).

---

## [v1.2.0] - Minigames & Bloodmoon Polish Update
### ✨ Fitur Baru
- **ColorGame Blocks Upgrade**: Memberikan 64 blok warna (color blocks) secara default kepada para pemain ColorGame.
- **Default Teleport Command**: Menjadikan command `/colorgame` secara default beraksi sebagai teleportasi instan ke arena game, serta menambahkan sub-command `/colorgame help`.
- **Insta-Break Slots**: Penambahan fitur fungsionalitas hancur instan (insta-break) pada mini-game slots untuk mempercepat alur permainan.

### ⚡ Peningkatan & Refactor
- **Bloodmoon Coins Integration**: Mengintegrasikan sistem mata uang virtual server (CoinsEngine) agar otomatis drop dari monster saat event Bloodmoon aktif.

### 🐛 Perbaikan Bug
- **Bloodmoon Bossbar for Late Joiners**: Memperbaiki BossBar Bloodmoon agar otomatis muncul bagi pemain yang baru bergabung ke server saat event sedang berjalan.
- **Shop GUI NPE Fix**: Memperbaiki NullPointerException pada sistem GUI Shop yang kerap memicu crash.
- **BloodmoonManager Duplicate Code**: Menghapus duplikasi baris kode pada `BloodmoonManager` yang memicu error saat kompilasi.
