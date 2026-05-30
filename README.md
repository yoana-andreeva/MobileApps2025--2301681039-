# UniPlanner — Академичен Органайзер

> Мобилно приложение за студенти и ученици, което помага за организиране на задачи, предмети и срокове.

---

## Идея

UniPlanner е Android приложение, създадено да замени тефтера и напомнянията на всеки студент. Позволява лесно добавяне на предмети и задачи, проследяване на срокове и получаване на навременни нотификации.

---

## Как работи

Приложението се състои от 4 основни екрана. На **началния екран (Dashboard)** потребителят вижда седмичен календар, статистика за изпълнените и чакащите задачи, и списък с предстоящи задачи филтриран по предмет. На екран **Задачи** се управляват всички задачи с възможност за филтриране по срок (днес, до 3 дни, тази седмица), редактиране и изтриване чрез swipe. Екран **Предмети** позволява добавяне на предмети с пастелен цвят. Екран **Календар** показва всички задачи по дата с цветни индикатори. Нотификациите се изпращат автоматично преди краен срок чрез WorkManager. Camera Intent позволява прикачване на снимка на записки към задача.

---

## Архитектура

Проектът следва **MVVM (Model-View-ViewModel) + Repository** архитектурен шаблон.

```
app/
├── data/
│   ├── local/          # Room DB — Entity, DAO, Database
│   │   ├── entity/     # Subject, Task
│   │   ├── dao/        # SubjectDao, TaskDao
│   │   └── Converters.kt
│   └── repository/     # UniPlannerRepository
├── di/                 # Hilt модули — AppModule
├── ui/
│   ├── adapter/        # TaskAdapter, SubjectAdapter
│   ├── calendar/       # CalendarFragment
│   ├── dashboard/      # DashboardFragment
│   ├── settings/       # SettingsBottomSheet
│   ├── subjects/       # SubjectsFragment
│   └── tasks/          # TasksFragment, AddEditTaskFragment
├── viewmodel/          # TaskViewModel, SubjectViewModel
└── worker/             # TaskReminderWorker, NotificationScheduler
```

### Технологии

| Технология | Версия | Употреба |
|---|---|---|
| Kotlin | 2.0.21 | Основен език |
| Android Gradle Plugin | 8.10.1 | Build система |
| Room | 2.6.1 | Локална база данни |
| Hilt | 2.51.1 | Dependency Injection |
| Navigation Component | 2.7.7 | Навигация между екрани |
| WorkManager | 2.9.0 | Нотификации |
| Coil | 2.6.0 | Зареждане на снимки |
| Material 3 | 1.12.0 | UI компоненти |
| MaterialCalendarView | 2.0.1 | Календар |

---

##  Потребителски поток

```
Стартиране
    └── Dashboard
          ├── Виж седмичния календар
          ├── Виж статистика (изпълнени/чакащи)
          ├── Филтрирай задачи по предмет
          └── Натисни дата → Календар с задачи

Добавяне на предмет
    └── Subjects → FAB (+) → Въведи име, преподавател, избери цвят → Добави

Добавяне на задача
    └── Tasks → FAB (+) → Въведи заглавие, описание, предмет, срок, приоритет
              → Опционално: снимай записки с камерата
              → Запази → Автоматична нотификация преди срока

Редактиране на задача
    └── Tasks → Натисни ️ → Промени данните → Запази промените

Изтриване на задача
    └── Tasks → Swipe наляво/надясно → Потвърди (или Отмени)

Настройки
    └── Bottom Nav ️ → Смени тема (светла/тъмна) → Изчисти стари задачи
```

---

## 🚀 Стъпки за стартиране

### Изисквания
- Android Studio Hedgehog или по-нова
- JDK 17
- Android устройство или емулатор с API 24+

### Стъпки

```bash
# 1. Клонирай репото
git clone https://github.com/yoana-andreeva/MobileApps2025--2301681039-.git

# 2. Отвори в Android Studio
File → Open → избери папката на проекта

# 3. Sync Gradle
File → Sync Project with Gradle Files

# 4. Стартирай
Run → Run 'app' (или натисни ▶)
```

---

## Тестове

### Unit тестове
```bash
./gradlew test
```
Покритие: **TaskViewModelTest**, **RepositoryTest** — покриват Repository и ViewModel логиката.

### UI тестове (Espresso)
```bash
./gradlew connectedAndroidTest
```
**MainActivityTest** — тества навигация, отваряне на AddTask екран и валидация.

---

## APK

Изтегли последната версия: [app-release.apk](apk/app-release.apk)

> Размер: < 60 MB | Min SDK: 24 | Target SDK: 35

---

## Автор

**Йоана Андреева** — Факултетен номер: 2301681039, СТД

