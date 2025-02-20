from django.shortcuts import render
from rest_framework import viewsets
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise
from .serializers import (
    PoseSerializer, 
    RoutineSerializer, 
    TrackingDetailSerializer,
    ExerciseDetailSerializer,
    RoutineConfigSerializer,
    RoutineExerciseSerializer,
)

class PoseViewSet(viewsets.ModelViewSet):
    queryset = Pose.objects.all()
    serializer_class = PoseSerializer

class RoutineViewSet(viewsets.ModelViewSet):
    queryset = Routine.objects.all()
    serializer_class = RoutineSerializer

class TrackingDetailViewSet(viewsets.ModelViewSet):
    queryset = TrackingDetail.objects.all()
    serializer_class = TrackingDetailSerializer

class ExerciseDetailViewSet(viewsets.ModelViewSet):
    queryset = ExerciseDetail.objects.all()
    serializer_class = ExerciseDetailSerializer

class RoutineExerciseViewSet(viewsets.ModelViewSet):
    queryset = RoutineExercise.objects.all()
    serializer_class = RoutineExerciseSerializer

class RoutineConfigViewSet(viewsets.ModelViewSet):
    queryset = RoutineConfig.objects.all()
    serializer_class = RoutineConfigSerializer