你好！我最近在写一个TODOLISTAPP的项目。它的要求与项目框架如下。具体代码细节如下。它目前是一个前端：andriodstudio+java；后端django的框架。


我目前已经完成了：添加、排序、筛选、云端同步等功能

我现在想做一个AI提建议的功能，其技术细节如下：

1. 在前端，修改日程详情界面，新增一个文本框+按钮，并适当美化。按钮是“问问PlanWiseAI的建议吧！”，点击按钮后，即可将此日程信息发送给API，然后API返回纯文本建议，映射到文本框中。
2. 文本框我希望是比较合适和美观
3. 在django后端中写一个API借口，来处理AI提建议功能。
4. 关于API功能，我希望接收日程信息，然后再日程信息内整合成一段合适的propmt，比如：“现在是某某时间，我计划在某某场地某某时间前完成某某事项，其细节是某某（备注内容），你能给我一些建议吗！我希望是类似于聊天框中的朴实文本内容与聊天语气，不希望你返回markdown格式文字“
5. 在这个AI调用中，可以用我的deepseekAPI，sk-76355a715ce345bc93e5b33431eb92fd
6. 你还需要看看这个功能需要对哪些JAVA类进行微调和更改

我希望你模拟成一个程序员，用步骤的方式带我一步一步细致入微地完成这一个功能。

要求每一步的更改都给出当前文件的完整代码。

同时我不希望你做任何额外的功能，只希望你能做好AI提建议功能即可。

做好后端并完成前后端API交互，最终实现功能。





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
