你好！我最近在写一个TODOLISTAPP的项目。它的要求与项目框架如下。具体代码细节可以看附件中的md文件。


我目前已经完成了：添加、排序、筛选等基础功能

也尝试做了依靠firebase做的登录与数据云同步功能。

但是在现在的情况下是我登陆后是不显示登录状态的，应该就是登陆失败了我很不理解！

帮我分析一下目前的代码应该怎么改





# Planwise 项目架构与细节代码

### 项目功能点需求

添加待办事项，为待办事项添加分类标签、时间、地点。

通过点击快速修改完成/未完成的待办事项状态。

按时间排序待办事项。

按类别筛选未完成的待办事项。

筛选某一时间段的待办事项。

查看全部已完成/未完成的待办事项。

将本地数据与云端同步。



### 项目框架

```cmd
app/
├── java/
│	├── data/
│	│   ├── db/
│	│   │   ├── AppDatabase.java
│	│   │   ├── DateConverter.java
│	│   │   └── ScheduleDao.java
│	│   ├── firebase/
│	│   │   ├── FirebaseAuthHelper.java
│	│   │   ├── FirestoreHelper.java
│	│   ├── model/
│	│   │   ├── Schedule.java
│	│   │   └── User.java
│	│   └── repository/
│	│       ├── ScheduleRepository.java
│	│       └── UserRepository.java
│	└── ui/
│		├── activity/
│		│   ├── AddScheduleActivity.java
│		│   ├── MainActivity.java
│		│   └── ScheduleDetailActivity.java
│		├── adapter/
│		│   └── ScheduleAdapter.java
│		├── fragment/
│		│   ├── CalendarFragment.java
│		│   ├── ProfileFragment.java
│		│   └── TodayTodoFragment.java
│		└── viewmodel/
│			├── ScheduleViewModel.java
│			└── UserViewModel.java
├── res/
│   ├── color/
│   ├── drawable/
│   ├── layout/
│   │   ├── activity_add_schedule.xml
│   │   ├── activity_main.xml
│   │   ├── activity_schedule_detail.xml
│   │   ├── dialog_login.xml
│   │   ├── dialog_sync_options.xml
│   │   ├── dialog_time_filter.xml
│   │   ├── fragment_calendar.xml
│   │   ├── fragment_profile.xml
│   │   ├── fragment_profile.xml
│   │   ├── fragment_today_todo.xml
│   │   └── item_schedule.xml
│   ├── menu/
│   │   ├── bottom_nav_menu.xml
│   │   └── menu_schedule_detail.xml
│   ├── mipmap
│   ├── navigation/
│   ├── values/
│   └── xml/
├── google-services.json
├── build.gradle.kts(Project)
├── build.gradle.kts(APP)
├── settings.gradle.kts
└── AndroidManifest.xml
```
