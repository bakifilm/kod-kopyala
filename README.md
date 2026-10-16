# Kod Kopyala

SMS ile gelen doğrulama kodlarını (OTP) bulup panoya kopyalayan küçük, açık kaynak Android uygulaması.
Google Messages olmadan (ör. Fossify Messages + HeliBoard ile) kod otomatik doldurma eksikliğini kapatır.

- İnternet izni **yok**, mesajlar hiçbir yere gönderilmez veya saklanmaz.
- Varsayılan olarak sadece Fossify Messages, Google Messages ve Samsung Messages bildirimlerini okur.
- Kodu otomatik panoya kopyalar ve "Kopyala" butonlu bir bildirim gösterir.
- HeliBoard'da "son kopyalananı öneri olarak göster" açıksa kod tek dokunuşla yazılır.

## Kurulum
1. [Releases](../../releases) sayfasından `KodKopyala.apk` indir ve kur.
2. Uygulamayı aç → **Bildirim erişimini aç** → Kod Kopyala'ya izin ver.
3. İzin gri/kilitliyse: App info → ⋮ → **Allow restricted settings**, sonra 2. adımı tekrarla.

Güncellemeleri otomatik almak için F-Droid'deki **Obtainium**'a bu repo linkini ekleyebilirsin.

## Derleme
GitHub Actions her `main` push'unda APK derler (Actions → son çalışma → Artifacts).
`v1.0` gibi bir etiket gönderildiğinde APK otomatik olarak Releases'a eklenir.

Kalıcı imza için repo Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
