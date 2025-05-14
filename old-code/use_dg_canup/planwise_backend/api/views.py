# api/views.py
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Schedule
from .serializers import ScheduleSerializer

@api_view(['GET', 'POST'])
def schedule_list(request):
    """
    获取所有日程或创建新日程
    """
    if request.method == 'GET':
        schedules = Schedule.objects.all()
        serializer = ScheduleSerializer(schedules, many=True)
        return Response(serializer.data)

    elif request.method == 'POST':
        serializer = ScheduleSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(['POST'])
def sync_schedules(request):
    """
    同步所有日程（覆盖现有数据）
    """
    if request.method == 'POST':
        # 先删除所有现有数据
        Schedule.objects.all().delete()
        
        # 批量创建新数据
        serializer = ScheduleSerializer(data=request.data, many=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)