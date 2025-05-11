# api/urls.py

from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ScheduleViewSet, sync_all_to_cloud, get_all_from_cloud

router = DefaultRouter()
router.register(r'schedules', ScheduleViewSet)

urlpatterns = [
    path('', include(router.urls)),
    path('sync-to-cloud/', sync_all_to_cloud, name='sync-to-cloud'),
    path('sync-from-cloud/', get_all_from_cloud, name='sync-from-cloud'),
]