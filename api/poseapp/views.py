from django.shortcuts import render
from rest_framework import viewsets
from .models import Pose, Routine
from .serializers import PoseSerializer, RoutineSerializer

class PoseViewSet(viewsets.ModelViewSet):
    queryset = Pose.objects.all()
    serializer_class = PoseSerializer

class RoutineViewSet(viewsets.ModelViewSet):
    queryset = Routine.objects.all()
    serializer_class = RoutineSerializer
    