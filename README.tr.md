# Hava

Kotlin ve Jetpack Compose ile yazılmış bir Android hava durumu uygulaması.

Tahmin [Open-Meteo](https://open-meteo.com) üzerinden geliyor; API anahtarı da
hesap da gerektirmiyor. Uygulamanın kendi sunucusu yok: bildiği her şey cihazda
duruyor. Bu yüzden ağ cevap vermeden önce ekranda hava durumu oluyor ve ağ
gittiğinde çalışmayı sürdürüyor.

*[English](README.md)*

| Liste | Bir şehir | Aynı şehrin gecesi | Bağlantı yokken |
| --- | --- | --- | --- |
| ![Yirmi şehir](docs/images/cities.png) | ![Bir şehir](docs/images/detail-day.png) | ![Gece üçte aynı şehir](docs/images/detail-night.png) | ![Çevrimdışı](docs/images/offline.png) |

## Fikir

**Arka plan süs değil, verinin kendisi.**

Her ekranın arkasındaki gökyüzü, o yerin havasından ve güneşin gerçek
konumundan hesaplanıyor; konum için NOAA Global Monitoring Laboratory'nin
yayımladığı algoritma kullanılıyor. Ay, evresine göre yerleştiriliyor ve bu
gece gerçekte sahip olduğu şekille çiziliyor. Hiçbiri hazır görsel değil,
hiçbiri hazır tema değil: uygulamanın bir temadan diğerine geçtiği bir an yok,
çünkü geçilecek bir tema yok.

Bundan iki sonuç çıkıyor ve kodun geri kalanının böyle görünmesinin sebebi bu.

**Hiçbir yerde hava durumu ikonu yok.** Listedeki kart zaten o yerin gökyüzü;
yanındaki sayıyı üreten ölçümden çiziliyor. Gerçek bir bulutun üstüne küçük bir
bulut resmi koymak, kartın zaten daha iyi söylediği şeyi tekrar etmek olurdu.
Yazıya kalan, resmin beceremediği kısım: ne kadar olduğu. Bu yüzden metin
*yağmurlu* değil *şiddetli yağmur* diyor.

**Saatlik eğri sürüklenebiliyor ve dünya onu izliyor.** Eğri boyunca hareket
etmek, ekranın altında görüldüğü saati değiştiriyor: ışık, güneşin yüksekliği,
yıldızlar, yağmur. Tahmin bir tablo olmaktan çıkıp içinde yürünebilen bir güne
dönüşüyor.

![Altı gökyüzü](docs/images/sky-engine.png)

## Okumaya değer kararlar

Hepsinin tam gerekçesi [docs/decisions](docs/decisions) altında.

| | |
| --- | --- |
| [Kontrast seçilmiyor, çözülüyor](docs/decisions/0001-contrast-is-computed.md) | Palet üretiliyor, dolayısıyla bir kez gözle onaylanamaz. İçeriği taşıyan cam, o gökyüzü üzerinde kılavuz oranına ulaşan **en düşük** opaklıkla karartılıyor. Bir test 10.752 üretilmiş paleti geziyor ve herhangi biri okunabilirliği kaybederse düşüyor. |
| [Güneş hesaplanıyor, taklit edilmiyor](docs/decisions/0002-real-solar-position.md) | Bir diski bildirilen gün doğumu ile batımı arasında kaydırmak çok daha az iş ve izleyenin görebileceği her açıdan yanlış. |
| [Tahmin geldiği gibi saklanıyor](docs/decisions/0003-store-the-response.md) | Bütün olarak okunup bütün olarak değiştiriliyor; normalleştirmek, tel biçimini alana çevirmenin ikinci bir yolunu eklerdi. |
| [Tek gökyüzü, gezinmenin üstünde](docs/decisions/0004-one-ambient-sky.md) | Ekranlar arasında geçmek havayı baştan başlatmamalı. |
| [Yalnızca yayımlanmış SDK'ya derleniyor](docs/decisions/0005-released-sdk-only.md) | En yeni AndroidX, henüz yayımlanmamış bir SDK istiyor. Yalnızca tek makinede derlenen bir depo, depo değildir. |

## Mimari

On üç modül. Convention plugin'lerle bağlanıyor; böylece yeni bir modül
eklemek, sonradan birbirinden uzaklaşacak yirmi satır yapılandırmayı kopyalamak
anlamına gelmiyor.

```
app  ──────────────  tek Activity, gezinme, ortam gökyüzü
│
├── feature:cities      yirmi yer, her biri kendi gökyüzünün altında
├── feature:detail      tek yer, saat saat
├── feature:favorites   saklanan yerler
├── feature:search      adıyla bulunan yer
│
├── core:ui             paylaşılan kart, hava durumunun sözcükleri
├── core:sky            arka plan: gök mekaniği, palet, katmanlar
├── core:designsystem   renk rolleri, tipografi, hareket, cam
├── core:data           depolar, çevrimdışı öncelikli
├── core:network        Open-Meteo ve tel biçiminin okunduğu tek yer
├── core:database       Room: yerler ve her biri için son tahmin
├── core:common         dispatcher'lar ve saat, ikisi de enjekte
└── core:model          alan modeli, Android tipi içermeyen düz Kotlin
```

Katmanların nasıl konuştuğu ve sınırların neden orada olduğu için
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Neler doğrulanıyor

```bash
./gradlew test    # birim testleri
./gradlew lint    # Android Lint; hata bulursa derlemeyi düşürür
```

En ağır basan testler, aritmetiği kendi önceki çıktısından başka bir şeye karşı
sınayanlar.

- **Güneş gök mekaniğine karşı sınanıyor.** En yüksek noktasında 90 dereceden
  enlem ile o günün deklinasyonu arasındaki farkı çıkarınca kalan yüksekliğe
  ulaşır ve meridyen üzerinde durur. Kutup dairesinde yaz ortası güneşi hiç
  batmaz, güney yarımkürede öğle güneşi kuzeyde durur, ekinoksta ekvatorda
  gündüz ile gece eşittir.
- **Palet okunabilirlik için sınanıyor:** dört şehir, dört tarih, her saat ve
  yayımlanmış her hava kodu. 10.752 gökyüzü; hiçbiri içeriği kontrast eşiğinin
  altına düşüremez ve hiçbiri havayı gizleyecek kadar opak bir cam isteyemez.
- **Tahmin ayrıştırıcısı canlı servisten alınmış bir cevapla çalışıyor**, elle
  yazılmış bir örnekle değil; çünkü elle yazılmış örnek yalnızca ayrıştırıcının
  onu yazan kişiyle hemfikir olduğunu sınar.

## Derleme

```bash
./gradlew :app:assembleDebug
```

JDK 17 veya üzeri ve Android SDK Platform 36 gerekiyor. Gradle toolchain
derleyiciyi JDK 21'e sabitliyor; böylece çıktı iş istasyonunda, derleme
sunucusunda ve ikinci bir makinede aynı oluyor.

Release derlemesi, depoya hiç girmeyen bir anahtarla imzalanıyor; anahtar ya
yok sayılan `keystore.properties` dosyasından ya da ortamdan okunuyor. İkisi de
yoksa release, debug anahtarıyla imzalanıyor. Yani bu depo herkes tarafından
klonlanıp çalışan bir pakete dönüştürülebilir. Üretilemeyecek olan tek şey,
gerçek kurulumun üzerine güncelleme olarak geçebilen bir paket.

## Sürüm çıkarma

Etiket göndermek derler, test eder, imzalar ve yayımlar:

```bash
git tag v1.0.0 && git push --tags
```

Hat, `gradle.properties` içinde bildirilen sürümle uyuşmayan bir etiketi
yayımlamayı reddediyor. İnsanların andığı adla cihazın karşılaştırdığı numaranın
birbirinden ayrıldığı bir sürüm, hiç sürüm çıkarmamaktan kötüdür.

## Erişilebilirlik

- Her kart ekran okuyucu için tek bir şeydir. Parça parça okunsa bir ad, bir
  sayı ve bir sözcük olurdu; birbirlerine ait olduklarını söyleyen hiçbir şey
  olmazdı.
- Animasyonu kaldıran cihaz ayarı temada bir kez okunuyor ve her bileşene
  veriliyor; böylece hiçbiri unutamıyor. Gökyüzünün her katmanı tek bir saatin
  fonksiyonu olduğu için, o saati durdurmak boş bir ekran değil doğru bir
  durağan görüntü bırakıyor.
- Kontrast varsayılmıyor, hesaplanıyor; ve hesap bir test.

## Teknoloji

| Konu | Seçim |
| --- | --- |
| Dil | Kotlin 2.4.20 |
| Arayüz | Jetpack Compose, Material 3 |
| Bağımlılık enjeksiyonu | Hilt |
| Ağ | Ktor, platform motoru, kotlinx.serialization |
| Depolama | Room |
| Derleme | Gradle 9.8.0, Android Gradle Plugin 9.4.1, convention plugin'ler |
| Asgari SDK | 26 (Android 8.0) |
| Derleme SDK'sı | 36 |

## Lisans

MIT. Bkz. [LICENSE](LICENSE). Pakete katılan Inter yazı tipi SIL Open Font
License altında kullanılıyor; koşulları `core/designsystem/licenses` içinde.
