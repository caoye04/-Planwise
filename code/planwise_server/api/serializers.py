# api/serializers.py

from rest_framework import serializers
from .models import Schedule

class ScheduleSerializer(serializers.ModelSerializer):
    isCompleted = serializers.BooleanField(source='is_completed')
    scheduledDate = serializers.DateTimeField(source='scheduled_date')
    
    class Meta:
        model = Schedule
        fields = ['id', 'scheduledDate', 'title', 'description', 'location', 
                  'category', 'isCompleted', 'priority', 'is_recurring', 
                  'recurring_pattern', 'created_at', 'updated_at']