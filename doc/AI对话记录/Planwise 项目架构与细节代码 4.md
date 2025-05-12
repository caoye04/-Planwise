你好！我最近在写一个TODOLISTAPP的项目。它的要求与项目框架如下。具体代码细节如下。它目前是一个前端：andriodstudio+java；后端django的框架。

我目前已经完成了：添加、排序、筛选、云端同步、AI提建议等功能

所有功能都已经基本完善啦！好耶

我现在想做一些界面逻辑的优化，重点是今日代办那边的界面与逻辑

1. 我希望把所有UI界面的今日待办词汇改成待办列表，因为我做筛选后其实是有很多非今日而是其他时间待办的事项情况的
2. 我希望选择筛选方式的这个ui这里可以加几个：今日待办、近三天待办、本周待办这种快捷选择方式
3. 在日程列表里的日程item这种ui里，可以看到标题、是否完成、标签、时间，但时间只显示了小时分钟，我希望在UI上显示成：”今天几点“，”还有几天“这种信息更突出的模式，但不需要更改数据库里的内容
4. 目前的标签只有个人和学习，我觉得太单调了，我希望可以多几个预设标签
5. 并且在创建待办的界面新增：“新增标签”的按钮和对应对话框，新增后会同步更新到UI中，包括根据标签筛选的那个UI

我希望你模拟成一个程序员，用步骤的方式带我一步一步细致入微地完成这一个功能。

要求每一步的更改都给出当前文件的完整代码。

同时我不希望你做任何额外的功能，只需要满足我的需求即可





# Planwise 项目架构与细节代码

### 项目功能点需求

添加待办事项，为待办事项添加分类标签、时间、地点。

通过点击快速修改完成/未完成的待办事项状态。

按时间排序待办事项。

按类别筛选未完成的待办事项。

筛选某一时间段的待办事项。

查看全部已完成/未完成的待办事项。

将本地数据与云端同步。

AI智能提建议



### 项目框架

```cmd
planwise_server/
├── api/
│   ├── migrations/
│   ├── __pycache__/
│   ├── admin.py
│   ├── apps.py
│   ├── models.py
│   ├── serializers.py
│   ├── tests.py
│   ├── urls.py
│   ├── views.py
│   └── __init__.py
├── planwise_server/
│   ├── __pycache__/
│   ├── asgi.py
│   ├── settings.py
│   ├── urls.py
│   ├── wsgi.py
│   └── __init__.py
├── db.sqlite3
└── manage.py
```

```cmd
planwise_frontend/
├── java/
│	├── data/
│	│   ├── db/
│	│   │   ├── AppDatabase.java
│	│   │   ├── DateConverter.java
│	│   │   └── ScheduleDao.java
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
│   │   ├── dialog_time_filter.xml
│   │   ├── fragment_calendar.xml
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
└── AndroidManifest.xml
```
