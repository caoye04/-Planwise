# api/models.py

from django.db import models

class Schedule(models.Model):
    title = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    scheduled_date = models.DateTimeField()
    location = models.CharField(max_length=255, blank=True, null=True)
    category = models.CharField(max_length=100)
    is_completed = models.BooleanField(default=False)
    priority = models.IntegerField(default=2)
    is_recurring = models.BooleanField(default=False)
    recurring_pattern = models.CharField(max_length=100, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title