你好！我最近在写一个TODOLISTAPP的项目。它的要求与项目框架如下。具体代码细节如下。它目前是一个andriodstudio+java的。


我目前已经完成了：添加、排序、筛选等基础功能

我现在想做云端同步这个功能

我现在想重新写这部分的逻辑，具体设计如下：

1. 去掉所有和用户有关的信息，默认只有一个用户“Planwiser”。这里可能需要你修改一下profile界面，去掉登录按钮啥的
2. 同时还有一个云端同步功能，在用户界面有两个按钮，一个是“将本地数据同步到云”，一个是“将云数据同步到本地”。当点击“将本地数据同步到云”按钮时，即将云数据清除，将本地数据存到云上；点击“将云数据同步到本地”时，即将本地数据清楚，将云数据放到本地数据位置，并且同时更新所有UI的对应数据。
3. 在不点击云同步按钮的时候，是离线的不需要任何不停云同步的功能，这只是点一下触发一下的功能
4. 关于云服务：这里的"云端"指部署后端服务的设备，**不要求必须使用云服务器**。可将后端服务部署在笔记本电脑上，只要手机和笔记本处于同一局域网即可实现前后端交互。推荐使用Django后端开发，通过RESTful API实现前后端交互。Django文档：[Django 文档 | Django documentation | Django](https://docs.djangoproject.com/zh-hans/5.2/)
5. 我的想法是手机和电脑再同一个局域网内的真机调试，所以只用管真机调试的URL
6. 我感觉这个功能就是文件的传输、下载、更新，应该不难吧！希望你写好

我希望你模拟成一个程序员，用步骤的方式带我一步一步细致入微地完成这一个功能。

要求每一步的更改都给出当前文件的完整代码。

同时我不希望你做任何额外的功能，只希望你能做好两个云同步按钮功能即可，将本地数据覆盖云数据+将运输局覆盖本地数据

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



### 项目框架

```cmd
app/
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
