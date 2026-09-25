<div align="center">

# Hava

**Arka planın süs değil, verinin kendisi olduğu bir Android hava durumu uygulaması.**

[![Derleme ve doğrulama](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/ci.yml/badge.svg)](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/ci.yml)
[![Sürüm](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/release.yml/badge.svg)](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/release.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Asgari SDK](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Lisans](https://img.shields.io/badge/lisans-MIT-blue)](LICENSE)

**Gençlik Kampı'nda, [Türkcell Kamp+](#bu-proje-nerede-yazıldı) Bilişim Kampı kapsamında yazıldı.**

[![Türkcell Kamp+](https://img.shields.io/badge/T%C3%BCrkcell%20Kamp%2B-Bili%C5%9Fim%20Kamp%C4%B1-FFC72C?labelColor=1a1a1a)](#bu-proje-nerede-yazıldı)
[![Program](https://img.shields.io/badge/Program-Mobil%20Uygulama%20Geli%C5%9Ftirme-FFC72C?labelColor=1a1a1a)](#bu-proje-nerede-yazıldı)
[![Turkcell Akademi](https://img.shields.io/badge/Turkcell-Akademi-FFC72C?labelColor=1a1a1a)](#bu-proje-nerede-yazıldı)

*[English](README.md)*

</div>

---

Tahmin [Open-Meteo](https://open-meteo.com) üzerinden geliyor; API anahtarı da
hesap da gerektirmiyor. Uygulamanın kendi sunucusu yok: bildiği her şey cihazda
duruyor. Bu yüzden ağ cevap vermeden önce ekranda hava durumu oluyor ve ağ
gittiğinde çalışmayı sürdürüyor.

| Liste | Bir şehir | Aynı şehrin gecesi | Bağlantı yokken |
| --- | --- | --- | --- |
| ![Yirmi şehir](docs/images/cities.png) | ![Bir şehir](docs/images/detail-day.png) | ![Gece üçte aynı şehir](docs/images/detail-night.png) | ![Çevrimdışı](docs/images/offline.png) |

## Bu proje nerede yazıldı

> **Bu uygulama, Gençlik Kampı'nda düzenlenen Türkcell Kamp+ Bilişim Kampı'nın
> Turkcell Akademi ile yürütülen Mobil Uygulama Geliştirme programında yazıldı.**
> Eylül 2026.

Kamp herkese aynı ödevi veriyor: **Kotlin** ve **Jetpack Compose** ile, ücretsiz
**Open-Meteo** servisine bağlanan **Hava** adlı bir hava durumu uygulaması. Üç
gün, beş anlatım, dört checkpoint, bir çalışan uygulama. Son gün her katılımcının
elinde yirmi şehir, saatlik ve günlük tahminleri olan bir detay ekranı, bütün
ekranlarda tutarlı gösterilen favoriler, dört asenkron durum (yükleniyor,
içerik, boş, hata) ve kendi telefonunda kurulu imzalı bir release paketi olmalı.

**Bu depo, o ödevin bir katılımcı tarafından verilmiş cevabı ve ödevin
istediğinin ötesine bilerek geçiyor.**

Amaç checkpoint'leri tikleyip bitirmek değil. Ödevin ortaya attığı ama sormadığı
daha zor bir soruyu cevaplamak: *bir gereksinimi karşılayan uygulamayı, insanın
telefonunda tutmayı seçtiği uygulamadan ayıran nedir?* Hava durumu uygulaması bu
soruyu sormak için iyi bir yer, çünkü gereksinim bitirilebilecek kadar küçük ama
işçiliğin tavanı yok. Bu yüzden ödevin şehir listesi, gök mekaniğinden
hesaplanan yirmi canlı gökyüzüne; durum makinesi, çevrimdışı öncelikli bir veri
katmanına; "telefonunda çalıştır" maddesi de doğrulayamadığı paketi yayımlamayı
reddeden imzalı bir sürüm hattına dönüştü.

Ödevde istenen her şey burada ve her checkpoint ölçütü kaynak kodda değil çalışan
bir cihazda denetlendi. Üstüne eklenenler [docs/decisions](docs/decisions)
altında kayıtlı; her biri, reddedilen daha ucuz seçenekle ve gerekçesiyle
birlikte. Çünkü alternatifi yazılmamış bir karar, karar değil tercihtir.

Kamp **Git'i de işin parçası** sayıyor, işin sonradan konulduğu bir yer olarak
değil. Bu deponun tek amaçlı commit'lerden oluşan doğrusal bir geçmişi, korumalı
bir `main` dalı, her pull request'te çalışan bir doğrulama hattı ve yalnızca
kaynakla uyuşan bir etiketten çıkarılabilen bir sürümü olmasının sebebi bu.
Hepsinin nasıl yürütüldüğü [CONTRIBUTING.md](CONTRIBUTING.md) içinde.

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
| [Gökyüzü acele etmez](docs/decisions/0006-the-sky-takes-its-time.md) | Havanın değişmesi, bir denetimin dokunuşa cevap vermesi değildir. Yeni bir yere varmak iki saniyeye yakın sürer; saatlik eğride parmağı izlemek saniyenin üçte biri. |

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

İkisi de her push'ta ve her pull request'te çalışıyor. Bir değişikliğin `main`
dalına ulaşması için ikisinin de geçmesi gerekiyor.

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

Hattın ötesinde bu projenin diğerlerinin üstünde tuttuğu tek bir kural var:
**bitti demeden önce ekranda çalıştır.** Yerleşim işleri, yan çevrilmiş bir
pencerede ve 1,8 yazı ölçeğinde bakılarak doğrulanıyor; çünkü yerleşimler oralarda
kırılıyor.

## Derleme

```bash
git clone https://github.com/OzcanOrhanDemirci/weather_app.git
cd weather_app
./gradlew :app:assembleDebug
```

JDK 17 veya üzeri ve Android SDK Platform 36 gerekiyor. Gradle toolchain
derleyiciyi JDK 21'e sabitliyor; böylece çıktı iş istasyonunda, derleme
sunucusunda ve ikinci bir makinede aynı oluyor. Yapılandırılacak bir şey yok:
anahtar yok, hesap yok, elle doldurulacak `local.properties` yok.

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
birbirinden ayrıldığı bir sürüm, hiç sürüm çıkarmamaktan kötüdür. Hat ayrıca
bitmiş paketin içindeki sertifikayı okuyor ve release anahtarı değilse duruyor.

Bütün sürümler [CHANGELOG.md](CHANGELOG.md) içinde; anahtara ne olduğu dahil tam
yordam [docs/RELEASE.md](docs/RELEASE.md) içinde.

## Erişilebilirlik

- Her kart ekran okuyucu için tek bir şeydir. Parça parça okunsa bir ad, bir
  sayı ve bir sözcük olurdu; birbirlerine ait olduklarını söyleyen hiçbir şey
  olmazdı.
- Animasyonu kaldıran cihaz ayarı temada bir kez okunuyor ve her bileşene
  veriliyor; böylece hiçbiri unutamıyor. Gökyüzünün her katmanı tek bir saatin
  fonksiyonu olduğu için, o saati durdurmak boş bir ekran değil doğru bir
  durağan görüntü bırakıyor.
- Kontrast varsayılmıyor, hesaplanıyor; ve hesap bir test.
- Arayüz içinde bulunduğu pencereye göre diziliyor, pencereye yayılmıyor: yan
  çevrilmiş telefonda şehirler iki sütun, bir şehir iki panel oluyor; büyük yazı
  ölçeğinde çakışacak etiketler ölçüyle aralanıyor.
- İki dil de eksiksiz. Gerçekten çevrilemeyecek bir dize, sebebini söyleyen bir
  yorumla işaretleniyor.

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

## Bu depoda nasıl çalışılıyor

| | |
| --- | --- |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Commit biçimi, dal adlandırma, bir pull request'in söylemesi gerekenler ve "bitti demeden önce çalıştır" kuralı. |
| [CHANGELOG.md](CHANGELOG.md) | Yayımlanmış her sürüm ve içinde ne değiştiği. |
| [SECURITY.md](SECURITY.md) | Uygulamanın nelere erişebildiği ve bir sorunun nasıl gizlice bildirileceği. |
| [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) | Contributor Covenant. |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Modül grafiği ve sınırların nerede olduğu. |
| [docs/RELEASE.md](docs/RELEASE.md) | Bir sürümün nasıl çıkarıldığı ve anahtara ne olduğu. |
| [docs/decisions](docs/decisions) | Alışılmadık parçaların neden öyle olduğu. |

`main` dalına doğrudan push kabul edilmiyor. Her değişiklik, doğrulama hattını
geçmiş bir pull request ile geliyor ve geçmiş doğrusal tutuluyor.

## Lisans

MIT. Bkz. [LICENSE](LICENSE). Pakete katılan Inter yazı tipi SIL Open Font
License altında kullanılıyor; koşulları `core/designsystem/licenses` içinde.

Hava verisi [Open-Meteo](https://open-meteo.com) tarafından sağlanıyor, CC BY 4.0
altında kullanılıyor.
