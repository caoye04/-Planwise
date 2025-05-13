> utf-8
# PlanWise 项目使用指南



## 项目简介

PlanWise 是一个功能完善的日程管理应用，支持添加、排序、筛选和云端同步等功能，并集成了 AI 智能建议功能。本文档将指导您如何在真机上调试和运行该项目。



## 环境要求

- Android Studio
- Python 3.8+
- Django 5.2+
- 与手机设备在同一局域网的电脑



## 项目结构

项目由两部分组成：

- 前端：Android Studio + Java
- 后端：Django REST API



## 后端服务器设置

### 1. 安装依赖

```cmd
cd planwise_server
pip install -r requirement.txt
```

### 2. 运行服务器

#### 查看本机 IP 地址

在运行服务器前，需要获取您电脑的局域网 IP 地址：

**Windows**:

```cmd
ipconfig
```

查找"IPv4 地址"字段，通常是 192.168.x.x 的形式。

**macOS/Linux**:

```
ifconfig
# or
ip addr
```

查找 en0（WiFi）或 en1（以太网）中的 inet 地址。

#### 启动 Django 服务器

```cmd
// cmd in planwise_server
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

使用 `0.0.0.0` 可以允许外部设备访问您的开发服务器。

### 3. 测试服务器

在浏览器中输入 `http://你的IP地址:8000/api/` 验证 API 是否正常工作。您应该能看到 Django REST framework 的默认 API 页面。



## Android 客户端设置

### 1. 修改 API 基础 URL

在 Android Studio 中打开 planwise_frontend 项目，找到 `ApiClient.java` 文件（位于 `com.example.planwise.data.api` 包下的第十四行），然后修改 `BASE_URL` 变量值：

```
 private static final String BASE_URL = "http://192.168.1.2:8000/"; // 替换为你电脑的局域网IP
```

### 2. 构建并运行

在 Android Studio 中点击运行即可。请注意确保手机和电脑连接到同一个WIFI并且确保防火墙未阻止端口 8000