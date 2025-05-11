from rest_framework import viewsets
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Schedule
from .serializers import ScheduleSerializer

class ScheduleViewSet(viewsets.ModelViewSet):
    queryset = Schedule.objects.all()
    serializer_class = ScheduleSerializer

@api_view(['GET'])
def api_overview(request):
    api_urls = {
        'List': '/schedules/',
        'Detail': '/schedules/<id>/',
        'Create': '/schedules/',
        'Update': '/schedules/<id>/',
        'Delete': '/schedules/<id>/',
    }
    return Response(api_urls)