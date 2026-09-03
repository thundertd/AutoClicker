# Tong hop cau truc code du an AutoClicker

Cap nhat lan quet: 2026-08-28

## 1. Tong quan

AutoClicker la ung dung Android/Kotlin dung `AccessibilityService` de gia lap click va swipe tren man hinh. Trang thai hien tai da mo rong tu app auto click don gian thanh ung dung quan ly "action": tao action, luu SQLite, dat diem click/swipe bang floating overlay, chay action, va xuat JSON.

Thong tin chinh:

- Package/namespace: `com.github.nestorm001.autoclicker`
- Module: `app`
- Android Gradle Plugin: `8.6.0`
- Kotlin: `2.0.20`
- `compileSdk`: `35`
- `targetSdkVersion`: `35`
- `minSdkVersion`: `24`
- Java/Kotlin target: Java 17
- UI/dependency chinh: AndroidX AppCompat, ConstraintLayout, RecyclerView, CardView, Material Components

## 2. Cau truc thu muc

```text
AutoClicker/
|-- build.gradle
|-- settings.gradle
|-- gradle.properties
|-- README.md
|-- README_VI.md
|-- Update.md
|-- COMPLETED_SUMMARY.md
|-- FEATURE_DOCUMENTATION.md
|-- IMPLEMENTATION_SUMMARY.md
|-- TONG_HOP_CAU_TRUC_CODE.md
|-- test_debug.ps1
|-- gradle/wrapper/
`-- app/
    |-- build.gradle
    |-- proguard-rules.pro
    `-- src/
        |-- main/
        |   |-- AndroidManifest.xml
        |   |-- java/com/github/nestorm001/autoclicker/
        |   |   |-- MainActivity.kt
        |   |   |-- ActionListActivity.kt
        |   |   |-- ActionDetailActivity.kt
        |   |   |-- ActionListAdapter.kt
        |   |   |-- GuideActivity.kt
        |   |   |-- TouchAndDragListener.kt
        |   |   |-- Toasts.kt
        |   |   |-- Extentions.kt
        |   |   |-- bean/
        |   |   |   |-- Action.kt
        |   |   |   `-- Event.kt
        |   |   |-- database/
        |   |   |   `-- ActionDatabaseHelper.kt
        |   |   `-- service/
        |   |       |-- AutoClickService.kt
        |   |       |-- FloatingClickService.kt
        |   |       |-- FloatingActionEditorService.kt
        |   |       `-- FloatingActionRunnerService.kt
        |   `-- res/
        |       |-- layout/
        |       |   |-- activity_main.xml
        |       |   |-- activity_action_list.xml
        |       |   |-- activity_action_detail.xml
        |       |   |-- activity_guide.xml
        |       |   |-- item_action.xml
        |       |   |-- floating_action_control.xml
        |       |   |-- floating_action_runner.xml
        |       |   |-- click_point_marker.xml
        |       |   |-- swipe_start_marker.xml
        |       |   |-- swipe_end_marker.xml
        |       |   |-- dialog_click_settings.xml
        |       |   |-- dialog_swipe_settings.xml
        |       |   `-- dialog_scenario_settings.xml
        |       |-- drawable/
        |       |-- values/
        |       |-- xml/
        |       `-- mipmap-*/
        |-- test/
        `-- androidTest/
```

## 3. Cau hinh build

### Root `build.gradle`

- Dinh nghia `kotlin_version = '2.0.20'`.
- Repository: `google()`, `mavenCentral()`.
- Classpath:
  - `com.android.tools.build:gradle:8.6.0`
  - `org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version`

### `app/build.gradle`

- Plugin: `com.android.application`, `kotlin-android`.
- Namespace va application id: `com.github.nestorm001.autoclicker`.
- Release build bat `minifyEnabled`, `shrinkResources`, ProGuard optimize, `zipAlignEnabled`.
- Product flavors: `local`, `googleplay`.
- `viewBinding true`, `buildConfig true`.
- Java/Kotlin toolchain 17.
- Da migrate sang AndroidX va Material Components.

## 4. Manifest va quyen

File: `app/src/main/AndroidManifest.xml`

Permissions:

- `SYSTEM_ALERT_WINDOW`: hien overlay/floating controls.
- `WRITE_EXTERNAL_STORAGE`: xuat JSON ra Downloads.
- `READ_EXTERNAL_STORAGE`: doc file ngoai neu can.

Thanh phan:

- `ActionListActivity`: launcher hien tai, hien danh sach action.
- `GuideActivity`: huong dan cap Accessibility permission.
- `ActionDetailActivity`: kiem tra quyen va khoi dong floating editor.
- `AutoClickService`: `AccessibilityService` thuc hien click/swipe bang gesture.
- `FloatingClickService`: widget ON/OFF cu.
- `FloatingActionEditorService`: overlay chinh sua action.
- `FloatingActionRunnerService`: overlay chay action da luu.

## 5. Model du lieu

### `bean/Action.kt`

`Action`:

- `id`
- `name`
- `clickPoints`
- `repeatCount`
- `delayBetweenClicks`
- `loopCount`: so vong lap toan bo action, `0` la lap vo han.
- `delayBetweenLoops`

`TargetType`:

- `CLICK`
- `SWIPE`

`ClickPoint`:

- Truong chung: `id`, `actionId`, `sequence`, `type`.
- CLICK: `x`, `y`, `clickCount`, `delayBefore`, `holdDuration`, `delayAfter`.
- SWIPE: `fromX`, `fromY`, `toX`, `toY`, `swipeDuration`.

`ActionSequence` va `SequenceItem` da co model cho tinh nang ghep chuoi action, nhung UI hien tai van TODO.

### `bean/Event.kt`

Model gesture cu gom `Event`, `Move`, `Click`, `Swipe`. `AutoClickService.run()` van ho tro model nay, nhung flow moi chu yeu dung `Action` va `ClickPoint`.

## 6. Database

File: `database/ActionDatabaseHelper.kt`

Database SQLite:

- Ten DB: `autoclicker.db`
- Version: `2`

Bang:

- `actions`: luu action, repeat count, delay, loop count.
- `click_points`: luu target click/swipe theo action.
- `action_sequences`: luu ten chuoi action.
- `sequence_items`: luu thu tu action trong chuoi.

API chinh:

- `insertAction(action)`
- `getAllActions()`
- `getActionById(actionId)`
- `updateAction(action)`
- `deleteAction(actionId)`
- `exportActionsToJson(actionIds)`
- `importActionsFromJson(jsonString)`

Ghi chu:

- `onUpgrade()` dang drop tat ca bang roi tao lai, co nguy co mat du lieu khi tang version.
- Export/import JSON chua bao phu day du schema moi, dac biet cac truong swipe, hold duration, delay before, loop count.

## 7. Activity

### `MainActivity.kt`

Activity don gian load `activity_main.xml`, bam `btnActionList` thi mo `ActionListActivity`. Tuy nhien manifest hien cau hinh `ActionListActivity` la launcher, nen `MainActivity` khong phai entry point chinh.

### `ActionListActivity.kt`

Man hinh trung tam:

- Hien danh sach action bang `RecyclerView`.
- Tao action moi bang dialog nhap ten.
- Mo `ActionDetailActivity` de chinh sua action.
- Chay action don le bang `FloatingActionRunnerService`.
- Chon nhieu action bang long click.
- Export mot action hoac nhieu action ra JSON.
- Kiem tra Accessibility permission va Overlay permission trong `onResume()`.
- Mo `GuideActivity` khi nguoi dung can huong dan cap quyen.

Tinh nang TODO:

- `btnCombineActions`: ghep action.
- `btnRunSequence`: chay loat/sequence.

### `ActionDetailActivity.kt`

Nhan `ACTION_ID`, load action tu SQLite, kiem tra quyen, roi start `FloatingActionEditorService`. Sau khi service duoc start, activity dua app ve background va finish de nguoi dung thao tac voi overlay.

### `GuideActivity.kt`

Man hinh huong dan cap Accessibility:

- Nut mo Accessibility Settings.
- Nut dong man hinh.
- Tu dong finish khi Accessibility permission da duoc bat.

## 8. RecyclerView adapter

### `ActionListAdapter.kt`

Adapter bind item action:

- Ten action: `tvActionName`.
- Thong tin so diem va so lan: `tvActionInfo`.
- Checkbox hien khi vao selection mode.
- Callback detail/export/run/long click/selection changed.

## 9. Service

### `AutoClickService.kt`

`AccessibilityService` thuc hien gesture:

- `autoClickService`: bien global nullable giu instance service hien tai.
- `click(x, y)`: click nhanh.
- `clickWithDuration(x, y, holdDuration)`: click co thoi gian giu.
- `swipe(fromX, fromY, toX, toY, duration)`: swipe.
- `run(newEvents)`: chay danh sach `Event` cu.

Tat ca gesture dung `dispatchGesture()`, nen can Android N/API 24 tro len.

### `FloatingActionEditorService.kt`

Overlay editor cho action:

- Hien menu noi `floating_action_control.xml`.
- Load target da luu tu DB.
- Them click point o giua man hinh.
- Them swipe bang hai lan cham: start va end.
- Hien marker click/swipe.
- Keo marker de doi toa do.
- Tap marker de mo dialog cau hinh.
- Xoa target cuoi va danh so lai.
- An/hien menu.
- Chay thu action trong editor.
- Luu action vao DB va thoat.

Dialog lien quan:

- `dialog_click_settings.xml`: delay before, hold duration, click count.
- `dialog_swipe_settings.xml`: delay before, swipe duration.
- `dialog_scenario_settings.xml`: loop count, delay between loops.

### `FloatingActionRunnerService.kt`

Overlay runner cho action da luu:

- Load action theo `ACTION_ID`.
- Hien panel `floating_action_runner.xml`.
- Cho keo panel bang `TouchAndDragListener`.
- Nut play bat dau thread chay action.
- Nut stop dung action.
- Cap nhat UI: action name, progress, total clicks, delay.

Ghi chu: runner hien chay theo `repeatCount`, `delayBetweenClicks`, `delayAfter` va click coordinates. Logic nay chua dong bo day du voi editor moi vi chua xu ly `TargetType.SWIPE`, `delayBefore`, `holdDuration`, `loopCount`.

### `FloatingClickService.kt`

Service cu tao widget `widget.xml` voi text `OFF/ON`:

- Tap de bat/tat timer.
- Timer lap moi 200 ms.
- Goi `autoClickService?.click(...)` gan vi tri widget.
- Flow UI moi khong con thay duong di chinh toi service nay, nhung service van duoc khai bao trong manifest.

## 10. Utility

### `TouchAndDragListener.kt`

Listener dung chung cho floating view:

- `ACTION_DOWN`: luu toa do ban dau.
- `ACTION_MOVE`: neu vuot nguong thi drag va update `WindowManager.LayoutParams`.
- `ACTION_UP`: neu khong drag thi goi callback click.

### `Toasts.kt`

Tien ich toast:

- Tai su dung mot instance `Toast`.
- Chi show tren main thread.
- Cung cap `errorToast`, `longToast`, `shortToast`.

### `Extentions.kt`

Tien ich:

- `logd()` va `loge()` chi log khi `BuildConfig.DEBUG`.
- `Context.dp2px()`.
- `typealias Action = () -> Unit`.

## 11. Resource UI

- `activity_action_list.xml`: danh sach action, top buttons, selection info, RecyclerView, FAB add.
- `activity_action_detail.xml`: man hinh loading khi khoi dong editor.
- `activity_guide.xml`: huong dan cap quyen.
- `item_action.xml`: item action voi detail/export/run/select.
- `floating_action_control.xml`: menu noi editor.
- `floating_action_runner.xml`: panel noi runner.
- `click_point_marker.xml`: marker click.
- `swipe_start_marker.xml`: marker start swipe.
- `swipe_end_marker.xml`: marker end swipe.
- `dialog_click_settings.xml`: cau hinh click.
- `dialog_swipe_settings.xml`: cau hinh swipe.
- `dialog_scenario_settings.xml`: cau hinh scenario loop.
- `widget.xml`: widget ON/OFF cu.
- `res/xml/config.xml`: cau hinh Accessibility Service voi `canPerformGestures="true"`.

## 12. Luong hoat dong tong the

```text
Mo app
  -> ActionListActivity
  -> kiem tra Accessibility va Overlay permission
  -> tao/chon action
     -> ActionDetailActivity
     -> FloatingActionEditorService
     -> dat click/swipe marker
     -> cau hinh target/scenario
     -> luu SQLite
  -> run action
     -> FloatingActionRunnerService
     -> AutoClickService.dispatchGesture()
  -> export action
     -> ActionDatabaseHelper.exportActionsToJson()
     -> ghi file JSON vao Downloads
```

## 13. Test va script

Test hien co:

- `ExampleUnitTest.kt`: test mau `2 + 2 = 4`.
- `ExampleInstrumentedTest.kt`: test mau Android.

Script:

- `test_debug.ps1`: script debug/test thu cong.

Chua thay test rieng cho SQLite CRUD, export/import JSON, permission flow, editor overlay, runner thread, swipe/click gesture va `TouchAndDragListener`.

## 14. Ghi chu ky thuat

- Nhieu chuoi tieng Viet trong Kotlin/XML dang bi loi encoding, co the hien thi sai tren UI.
- `ActionListActivity` la launcher hien tai; `MainActivity` co ve la man hinh cu/phu.
- Co hai flow runner: editor runner moi va `FloatingActionRunnerService`; logic chua dong bo hoan toan.
- Storage permission cu co the khong phu hop voi target SDK 35; nen can nhac Storage Access Framework hoac API luu file phu hop.
- `autoClickService` nullable nen neu Accessibility Service chua connected, lenh click/swipe se bi bo qua.
- Mot so tinh nang da co model/database nhung UI van TODO: action sequence, combine actions, run sequence.

