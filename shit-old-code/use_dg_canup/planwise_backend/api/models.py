# api/models.py
from django.db import models

class Schedule(models.Model):
    id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    scheduledDate = models.DateTimeField()
    location = models.CharField(max_length=255, blank=True, null=True)
    category = models.CharField(max_length=100, blank=True, null=True)
    isCompleted = models.BooleanField(default=False)
    priority = models.IntegerField(default=2)
    isRecurring = models.BooleanField(default=False)
    recurringPattern = models.CharField(max_length=50, blank=True, null=True)
    lastSynced = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title