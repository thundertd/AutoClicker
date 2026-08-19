# Tong hop cau truc code du an AutoClicker

## 1. Tong quan

AutoClicker la ung dung Android viet bang Kotlin. Ung dung tao mot nut noi tren man hinh, cho phep nguoi dung bat/tat viec click tu dong vao mot vi tri gan nut noi. Co che click duoc thuc hien thong qua `AccessibilityService` va API `dispatchGesture`, nen tinh nang click tu dong chi kha dung tren Android N/API 24 tro len.

Thong tin chinh:

- Package: `com.github.nestorm001.autoclicker`
- Module chinh: `app`
- Ngon ngu: Kotlin
- **Android Gradle Plugin: `8.6.0`** (cap nhat 2026)
- **Kotlin: `2.0.20`** (cap nhat 2026)
- **Gradle: `8.10`** (cap nhat 2026)
- **Java Version: `26`** (cap nhat 2026)
- **`compileSdk`: `35`** (Android 15) (cap nhat 2026)
- `minSdkVersion`: `24` trong `defaultConfig`
- **`targetSdk`: `35`** (Android 15) (cap nhat 2026)
- **Da migrate sang AndroidX** (cap nhat 2026)

### Cac cap nhat chinh (2026)

Project da duoc nang cap toan dien:

✅ **Java 26** - Phien ban Java moi nhat  
✅ **Android 15 (API 35)** - Compile SDK va Target SDK moi nhat  
✅ **Gradle 8.10** - Ho tro day du Java 26  
✅ **Android Gradle Plugin 8.6.0** - Phien ban AGP moi nhat  
✅ **Kotlin 2.0.20** - Phien ban Kotlin moi nhat  
✅ **AndroidX** - Migration hoan tat, thay the tat ca Android Support Library  
✅ **ViewBinding** - Thay the kotlin-android-extensions da deprecated  
✅ **MavenCentral** - Thay the jcenter() da deprecated  

## 2. Cau truc thu muc

```text
AutoClicker/
|-- .gitignore
|-- README.md
|-- build.gradle
|-- gradle.properties
|-- gradlew
|-- gradlew.bat
|-- settings.gradle
|-- gradle/
|   `-- wrapper/
|       |-- gradle-wrapper.jar
|       `-- gradle-wrapper.properties
`-- app/
    |-- build.gradle
    |-- proguard-rules.pro
    `-- src/
        |-- main/
        |   |-- AndroidManifest.xml
        |   |-- ic_launcher-web.png
        |   |-- java/
        |   |   `-- com/github/nestorm001/autoclicker/
        |   |       |-- Extentions.kt
        |   |       |-- MainActivity.kt
        |   |       |-- Toasts.kt
        |   |       |-- TouchAndDragListener.kt
        |   |       |-- bean/
        |   |       |   `-- Event.kt
        |   |       `-- service/
        |   |           |-- AutoClickService.kt
        |   |           `-- FloatingClickService.kt
        |   `-- res/
        |       |-- drawable/
        |       |   `-- widget_background.xml
        |       |-- layout/
        |       |   |-- activity_main.xml
        |       |   `-- widget.xml
        |       |-- mipmap-*/
        |       |   `-- launcher icons
        |       |-- values/
        |       |   |-- colors.xml
        |       |   |-- strings.xml
        |       |   `-- styles.xml
        |       `-- xml/
        |           `-- config.xml
        |-- test/
        |   `-- java/com/github/nestorm001/autoclicker/ExampleUnitTest.kt
        `-- androidTest/
            `-- java/com/github/nestorm001/autoclicker/ExampleInstrumentedTest.kt
```

## 3. File cau hinh build

### `settings.gradle`

Khai bao mot module duy nhat:

```gradle
include ':app'
```

### `build.gradle` (root)

File build cap root, khai bao:

- Repository: `google()`, `mavenCentral()` (da thay the `jcenter()` cu)
- Android Gradle Plugin: `com.android.tools.build:gradle:8.6.0`
- Kotlin Gradle Plugin: `org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20`
- Task `clean`

### `gradle/wrapper/gradle-wrapper.properties`

Cau hinh Gradle wrapper:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-all.zip
```

Gradle 8.10 ho tro Java 26 va cac tinh nang moi nhat.

### `gradle.properties`

Cau hinh project-wide:

```properties
# Cau hinh JVM cho Gradle daemon
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# Bat AndroidX
android.useAndroidX=true

# Tu dong chuyen thu vien cu sang AndroidX
android.enableJetifier=true

# Kotlin code style
kotlin.code.style=official
```

### `app/build.gradle`

File build cua module ung dung Android:

- Plugins (dung DSL moi):
  - `com.android.application`
  - `kotlin-android`
  - **Da go `kotlin-android-extensions`** (deprecated)
- `namespace`: `com.github.nestorm001.autoclicker` (bat buoc tu AGP 8.x)
- `applicationId`: `com.github.nestorm001.autoclicker`
- **Java compatibility: `JavaVersion.VERSION_26`**
- **Kotlin JVM target: `26`**
- Build types:
  - `debug`: bat `debuggable`
  - `release`: bat `minifyEnabled`, `shrinkResources`, `zipAlignEnabled`, tat `debuggable`
- Product flavors:
  - `local`: gioi han resource `en`, `xxhdpi`, dat `minSdkVersion 24`
  - `googleplay`
- Build features:
  - `viewBinding = true` (thay the cho kotlin-android-extensions)
- Dependencies chinh (da migrate sang AndroidX):
  - Kotlin stdlib: `org.jetbrains.kotlin:kotlin-stdlib:2.0.20`
  - AppCompat: `androidx.appcompat:appcompat:1.7.0`
  - ConstraintLayout: `androidx.constraintlayout:constraintlayout:2.1.4`
  - Core KTX: `androidx.core:core-ktx:1.13.1`
  - JUnit: `junit:junit:4.13.2`
  - AndroidX Test: `androidx.test.ext:junit:1.2.1`
  - Espresso: `androidx.test.espresso:espresso-core:3.6.1`

### `app/proguard-rules.pro`

Hien tai chi chua template mac dinh, chua co rule rieng cho du an.

## 3.1. Huong dan build project

### Yeu cau

- **Java Development Kit (JDK) 26** hoac cao hon
- **Android SDK** voi:
  - Android SDK Platform 35 (Android 15)
  - Android SDK Build-Tools 35.x
- **Android Studio** (phien ban moi nhat, khuyen nghi)
- Hoac **Gradle** 8.10+ va Android SDK tu command line

### Cach 1: Build bang Android Studio

1. **Mo project:**
   ```
   Android Studio > File > Open > chon thu muc AutoClicker
   ```

2. **Dong bo Gradle:**
   - Android Studio tu dong dong bo Gradle khi mo project
   - Neu khong, nhan nut "Sync Project with Gradle Files" (bieu tuong voi)

3. **Build APK:**
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```
   - File APK se duoc tao trong `app/build/outputs/apk/`

4. **Build flavor cu the:**
   - Build > Select Build Variant > chon `localDebug`, `localRelease`, `googleplayDebug`, hoac `googleplayRelease`
   - Sau do build APK hoac run

5. **Chay tren thiet bi/emulator:**
   ```
   Run > Run 'app' (hoac nhan Shift+F10)
   ```

### Cach 2: Build bang command line (Windows)

1. **Build APK debug:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```
   - APK: `app\build\outputs\apk\local\debug\` va `googleplay\debug\`

2. **Build APK release:**
   ```powershell
   .\gradlew.bat assembleRelease
   ```
   - APK: `app\build\outputs\apk\local\release\` va `googleplay\release\`

3. **Build flavor cu the:**
   ```powershell
   # Build local debug
   .\gradlew.bat assembleLocalDebug
   
   # Build local release
   .\gradlew.bat assembleLocalRelease
   
   # Build googleplay debug
   .\gradlew.bat assembleGoogleplayDebug
   
   # Build googleplay release
   .\gradlew.bat assembleGoogleplayRelease
   ```

4. **Clean project:**
   ```powershell
   .\gradlew.bat clean
   ```

5. **Chay test:**
   ```powershell
   # Unit tests
   .\gradlew.bat test
   
   # Instrumented tests (can thiet bi/emulator ket noi)
   .\gradlew.bat connectedAndroidTest
   ```

6. **Cai dat truc tiep len thiet bi:**
   ```powershell
   # Cai dat debug build
   .\gradlew.bat installLocalDebug
   
   # Hoac googleplay flavor
   .\gradlew.bat installGoogleplayDebug
   ```

### Cach 3: Build bang command line (Linux/Mac)

Tuong tu Windows nhung dung `./gradlew` thay vi `.\gradlew.bat`:

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Build flavor cu the
./gradlew assembleLocalRelease

# Clean
./gradlew clean

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Install
./gradlew installLocalDebug
```

### Ghi chu quan trong

1. **Ky APK cho release:**
   - APK release can duoc ky bang signing key de cai dat len thiet bi
   - Cau hinh trong `app/build.gradle` hoac tao keystore va ky thu cong:
     ```powershell
     jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-key.keystore app-release.apk my-key-alias
     ```

2. **Java 26:**
   - Dam bao bien moi truong `JAVA_HOME` tro den JDK 26
   - Kiem tra phien ban: `java -version`
   - Neu Gradle khong tim thay JDK 26, co the chi dinh trong `gradle.properties`:
     ```properties
     org.gradle.java.home=C:/Program Files/Java/jdk-26
     ```

3. **Loi build:**
   - Neu gap loi ve dependency, thu chay: `.\gradlew.bat --refresh-dependencies`
   - Neu gap loi cache, thu: `.\gradlew.bat clean build --no-build-cache`

4. **Xem cac task kha dung:**
   ```powershell
   .\gradlew.bat tasks
   ```

## 4. Android Manifest va quyen

File: `app/src/main/AndroidManifest.xml`

**Cap nhat 2026:**
- Da go thuoc tinh `package` tu manifest (chuyen sang khai bao `namespace` trong build.gradle theo yeu cau cua AGP 8.x+)

Ung dung khai bao quyen:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```

Quyen nay dung de hien thi nut noi tren man hinh bang `WindowManager`.

Thanh phan duoc khai bao:

- `MainActivity`
  - Activity launcher cua app
  - `launchMode="singleInstance"`
  - Xu ly thay doi orientation bang `configChanges="orientation"`
- `AutoClickService`
  - Ke thua `AccessibilityService`
  - Can permission `android.permission.BIND_ACCESSIBILITY_SERVICE`
  - Gan metadata `@xml/config`
- `FloatingClickService`
  - Service thong thuong dung de hien thi floating widget
  - `exported="false"`

## 5. Resource chinh

### `res/xml/config.xml`

Cau hinh cho `AutoClickService`:

- Lang nghe tat ca accessibility event bang `typeAllMask`
- Feedback type: `feedbackGeneric`
- Cho phep gesture bang `android:canPerformGestures="true"`
- Package target: `com.github.nestorm001.autoclicker`
- Mo ta service lay tu `@string/accessibility_service_description`

### `res/layout/activity_main.xml`

Layout cua man hinh chinh. Giao dien rat don gian:

- `androidx.constraintlayout.widget.ConstraintLayout` full man hinh (da migrate sang AndroidX)
- Mot `Button` co id `@+id/button`
- Text cua button: `start`
- Button nam o goc duoi ben phai

### `res/layout/widget.xml`

Layout cua floating widget:

- Root la `TextView`
- Id cung la `@+id/button`
- Background: `@drawable/widget_background`
- Text mac dinh: `OFF`
- Khi dang auto click se doi thanh `ON`

### `res/values/strings.xml`

Chua:

- Ten app: `AutoClicker`
- Ten service accessibility: `AutoClickService`
- Id service accessibility: `com.github.nestorm001.autoclicker/.service.AutoClickService`
- Mo ta accessibility service hien dang la `TODO`

### `res/values/colors.xml` va `styles.xml`

Khai bao mau theme AppCompat va mau nen widget.

## 6. Cac lop Kotlin chinh

### `MainActivity.kt`

Vai tro:

- Hien thi layout `activity_main`
- Xu ly nut `start` bang `findViewById<Button>(R.id.button)` (thay the cho kotlin-android-extensions)
- Kiem tra quyen ve Accessibility Service
- Kiem tra/xin quyen ve overlay window
- Khoi dong `FloatingClickService`
- Dua task ve background sau khi khoi dong nut noi
- Don dep service khi activity bi huy

Luon chinh:

1. `onCreate()` gan click listener cho button.
2. Khi bam button:
   - Neu Android < N hoac da co quyen overlay, tao intent den `FloatingClickService`, goi `startService()`, roi dua app ve background.
   - Neu chua co quyen overlay, goi `askPermission()` va hien toast.
3. `onResume()`:
   - Goi `checkAccess()` de xem `AutoClickService` da duoc bat trong Accessibility Settings chua.
   - Neu chua bat, mo man hinh `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
   - Neu Android M tro len va chua co overlay permission, mo man hinh xin quyen overlay.
4. `onDestroy()`:
   - Dung floating service neu da khoi dong.
   - Dung/disable `AutoClickService` neu dang ton tai.

### `AutoClickService.kt`

Vai tro:

- La `AccessibilityService` thuc hien thao tac click/gesture tren man hinh.
- Giu bien global `autoClickService` de cac thanh phan khac co the goi click.

Thanh phan chinh:

- `var autoClickService: AutoClickService? = null`
  - Luu instance service hien tai.
- `onServiceConnected()`
  - Gan `autoClickService = this`
  - Mo lai `MainActivity` bang flag `FLAG_ACTIVITY_NEW_TASK`
- `click(x: Int, y: Int)`
  - Neu Android < N thi return.
  - Tao `Path` tai toa do `(x, y)`.
  - Tao `GestureDescription.StrokeDescription`.
  - Goi `dispatchGesture()` de click.
- `run(newEvents: MutableList<Event>)`
  - Nhan danh sach event tuy bien.
  - Tao gesture tu cac event va dispatch mot lan.
- `onUnbind()` va `onDestroy()`
  - Reset `autoClickService = null`.

### `FloatingClickService.kt`

Vai tro:

- Tao nut noi tren man hinh.
- Cho phep keo nut noi.
- Bam nut noi de bat/tat auto click.
- Khi bat, timer lap lai moi 200 ms va yeu cau `AutoClickService` click vao vi tri gan nut noi.

Thanh phan chinh:

- `WindowManager`: quan ly view noi.
- `view`: layout `R.layout.widget`.
- `params`: `WindowManager.LayoutParams` cua floating view.
- `timer`: lap lich click tu dong.
- `isOn`: trang thai ON/OFF cua widget.

Luon chinh:

1. `onCreate()`:
   - Chuyen `10dp` sang pixel de lam nguong bat dau drag.
   - Inflate `widget.xml`.
   - Chon window type:
     - Android O tro len: `TYPE_APPLICATION_OVERLAY`
     - Cu hon: `TYPE_PHONE`
   - Them view vao `WindowManager`.
   - Gan `TouchAndDragListener` de phan biet tap va drag.
2. `viewOnClick()`:
   - Neu dang ON: huy timer.
   - Neu dang OFF: tao `fixedRateTimer` chu ky 200 ms.
   - Moi lan timer chay:
     - Lay toa do view tren man hinh.
     - Goi `autoClickService?.click(...)`.
   - Doi text widget thanh `ON` hoac `OFF`.
3. `onDestroy()`:
   - Huy timer.
   - Go view khoi `WindowManager`.
4. `onConfigurationChanged()`:
   - Hoan doi/lap lai toa do da ghi nhan khi xoay man hinh va cap nhat layout.

### `TouchAndDragListener.kt`

Vai tro:

- Xu ly su kien cham cho floating widget.
- Phan biet mot lan tap voi hanh dong keo.

Co che:

- `ACTION_DOWN`: luu toa do ban dau cua window va toa do cham.
- `ACTION_MOVE`:
  - Tinh khoang cach di chuyen.
  - Neu vuot nguong `startDragDistance`, xem la drag.
  - Cap nhat `params.x`, `params.y`, goi callback `onDrag`.
- `ACTION_UP`:
  - Neu khong phai drag, goi callback `onTouch`.

### `Event.kt`

Vai tro:

- Dinh nghia model cho cac thao tac gesture co the chay boi `AutoClickService.run()`.

Cac lop:

- `Event`
  - Lop abstract.
  - Co `startTime`, `duration`, `path`.
  - Ham `onEvent()` tao `GestureDescription.StrokeDescription`.
- `Move`
  - Di chuyen path den mot diem.
- `Click`
  - Tao thao tac click tai mot diem.
- `Swipe`
  - Tao duong vuot tu diem `from` den diem `to`.

Ghi chu: hien tai luong UI chinh chi dung `click(x, y)` trong `AutoClickService`; danh sach `Event` mo duong cho cac tinh nang gesture/multi-action sau nay.

### `Extentions.kt`

Vai tro:

- Chua cac ham tien ich/extension.

Noi dung:

- `Any.logd()` va `Any.loge()`
  - Ghi log khi `BuildConfig.DEBUG = true`.
- `Context.dp2px()`
  - Doi dp sang pixel.
- `typealias Action = () -> Unit`
  - Alias cho callback khong tham so.

### `Toasts.kt`

Vai tro:

- Gom cac ham hien toast.

Noi dung:

- Dung mot bien `toast` global de tai su dung Toast, tranh tao nhieu toast lien tiep.
- Chi hien toast neu dang o main thread.
- Cac ham public noi bo:
  - `errorToast(e: Throwable)`
  - `longToast(text: String)`
  - `longToast(@StringRes id: Int)`
  - `shortToast(text: String)`
  - `shortToast(@StringRes id: Int)`
- Annotation `@Duration` gioi han gia tri duration cua Toast.

## 7. Luong hoat dong tong the

```text
Nguoi dung mo app
        |
        v
MainActivity.onResume()
        |
        |-- Neu chua bat Accessibility Service
        |       -> mo Accessibility Settings
        |
        |-- Neu chua co overlay permission
                -> mo man hinh xin quyen overlay

Nguoi dung bam nut "start"
        |
        v
MainActivity.startService(FloatingClickService)
        |
        v
FloatingClickService tao nut noi OFF tren man hinh
        |
        |-- Keo nut noi
        |       -> TouchAndDragListener cap nhat WindowManager.LayoutParams
        |
        `-- Bam nut noi
                -> doi OFF/ON
                -> neu ON, timer chay moi 200 ms
                -> timer lay toa do nut noi
                -> autoClickService?.click(x, y)
                -> AutoClickService.dispatchGesture()
```

## 8. Dieu kien quyen va phien ban Android

Ung dung can hai dieu kien de hoat dong dung:

1. Accessibility Service phai duoc bat trong cai dat he thong.
2. Quyen hien thi tren ung dung khac (`SYSTEM_ALERT_WINDOW`) phai duoc cap.

Click tu dong bang `dispatchGesture()` chi hoat dong tren Android N/API 24 tro len. Trong code, cac ham click/gesture deu return neu thiet bi thap hon Android N.

## 9. Test

Du an hien co test mac dinh:

- `ExampleUnitTest.kt`: test `2 + 2 = 4`
- `ExampleInstrumentedTest.kt`: test mau Android instrumented (da migrate sang AndroidX test framework)

**Cap nhat 2026:**
- Test instrumented da migrate sang `androidx.test.ext.junit.runners.AndroidJUnit4`
- Dung `InstrumentationRegistry.getInstrumentation().targetContext` thay vi `InstrumentationRegistry.getTargetContext()`

Chua co test rieng cho:

- Logic phan biet tap/drag trong `TouchAndDragListener`
- Luong xin quyen
- Luong bat/tat timer click
- Tao gesture trong `AutoClickService`

## 10. Ghi chu ky thuat

### Cap nhat 2026:
- **Da migrate sang AndroidX**: tat ca thu vien Android Support Library da duoc thay the bang AndroidX
- **Da go `kotlin-android-extensions`**: thay bang `findViewById()` hoac co the dung ViewBinding
- **Da thay the `jcenter()`**: chuyen sang `mavenCentral()` do jcenter da deprecated
- **Nang cap len Java 26**: ho tro cac tinh nang Java moi nhat
- **Nang cap len Android 15 (API 35)**: compileSdk va targetSdk deu la 35
- **Nang cap Gradle len 8.10**: ho tro day du cho Java 26 va Android Gradle Plugin 8.6
- **Nang cap Kotlin len 2.0.20**: ho tro cac tinh nang Kotlin moi nhat
- **Dung plugins DSL**: thay the `apply plugin` bang `plugins {}` block (modern Gradle)
- **Them namespace trong build.gradle**: bat buoc tu Android Gradle Plugin 8.0+
- **Cap nhat test framework**: dung androidx.test thay vi android.support.test

### Ghi chu cu van con hieu luc:
- Mot so comment trong code dang bi loi encoding.
- `strings.xml` van de `accessibility_service_description` la `TODO`, nen mo ta service trong Settings chua ro rang.
- `AutoClickService` duoc luu trong bien global nullable, nen cac noi goi `autoClickService?.click()` se im lang bo qua neu service chua ket noi.
- Floating widget click vao toa do tinh tu vi tri cua chinh widget: `location[0] + view.right + 10`, `location[1] + view.bottom + 10`.

### Khuyen nghi cai tien tuong lai:
- Chuyen sang dung ViewBinding day du thay vi `findViewById()` trong tat ca Activity/Fragment
- Them proper signing configuration cho release builds
- Viet them unit test va instrumented test cho cac tinh nang chinh
- Lam ro accessibility service description trong strings.xml
- Cai thien xu ly error va edge cases
- Co the them them tinh nang tu tuy chinh chu ky click, so lan click, pattern click, etc.

