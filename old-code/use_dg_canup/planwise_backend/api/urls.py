# api/urls.py
from django.urls import path
from . import views

urlpatterns = [
    path('schedules/', views.schedule_list, name='schedule-list'),
    path('sync/', views.sync_schedules, name='sync-schedules'),
]