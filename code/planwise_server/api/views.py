# api/views.py

from rest_framework import viewsets, status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Schedule
from .serializers import ScheduleSerializer

class ScheduleViewSet(viewsets.ModelViewSet):
    queryset = Schedule.objects.all()
    serializer_class = ScheduleSerializer

@api_view(['POST'])
def sync_all_to_cloud(request):
    """接收客户端的所有Schedule数据，替换服务器上的所有数据"""
    if request.method == 'POST':
        # 清除所有现有数据
        Schedule.objects.all().delete()
        
        # 如果是空列表，直接返回成功
        if not request.data:
            return Response({"message": "所有数据已清除"}, status=status.HTTP_200_OK)
        
        # 批量创建新数据
        serializer = ScheduleSerializer(data=request.data, many=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(['GET'])
def get_all_from_cloud(request):
    """返回服务器上的所有Schedule数据"""
    if request.method == 'GET':
        schedules = Schedule.objects.all()
        serializer = ScheduleSerializer(schedules, many=True)
        return Response(serializer.data)