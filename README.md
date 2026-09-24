# TodoApp — Aplicativo de Tarefas (Kotlin / Android)

Aplicativo móvel de lista de tarefas com persistência SQLite (Room), categorias, filtros, datas de vencimento e notificações locais.

## Requisitos

- Android Studio Ladybug (2024.2+) ou superior
- JDK 17
- Android SDK 35
- minSdk 26

## Como abrir e executar

1. Abra o Android Studio
2. **File → Open** e selecione a pasta `TodoApp`
3. Aguarde o Gradle Sync
4. Conecte um dispositivo/emulador (API 26+)
5. Clique em **Run**

## Funcionalidades

- CRUD de tarefas (criar, editar, excluir, concluir/reabrir)
- CRUD de categorias
- Filtros por status (Todas / Pendentes / Concluídas) e por categoria
- Data/hora de vencimento opcional
- Notificações locais agendadas (AlarmManager)
- Persistência SQLite via Room
- Dados sobrevivem a fechamento/reabertura do app e reboot (BootReceiver reagenda)

## Estrutura

```
app/src/main/java/com/example/todoapp/
├── data/
│   ├── entity/       # Task, Category
│   ├── dao/          # TaskDao, CategoryDao
│   ├── repository/   # TodoRepository
│   └── AppDatabase.kt
├── ui/
│   ├── screens/      # TaskList, TaskEditor, Category
│   ├── navigation/   # NavGraph
│   └── theme/
├── viewmodel/
├── util/             # NotificationHelper, Receivers
├── MainActivity.kt
└── TodoApplication.kt
```

## Arquitetura

- **MVVM** com ViewModels + StateFlow
- **Repository** como camada de abstração do Room
- **Jetpack Compose** + Navigation Compose
- **Room** para SQLite
- **AlarmManager** + BroadcastReceiver para notificações locais

## Estratégia de exclusão de categoria

ForeignKey `ON DELETE SET NULL` — tarefas que usavam a categoria ficam com `categoryId = null`.
