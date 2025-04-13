from django.shortcuts import render
from django.db.models import Prefetch
from django.core.cache import cache
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.decorators import action
from .models import (
    Pose, 
    Routine, 
    TrackingDetail, 
    ExerciseDetail, 
    RoutineConfig, 
    RoutineExercise, 
    RepData, 
    RoutineComponentData, 
    RoutineData,
    Patient
)
from .serializers import (
    PoseSerializer, 
    RoutineSerializer, 
    TrackingDetailSerializer,
    ExerciseDetailSerializer,
    RoutineConfigSerializer,
    RoutineExerciseSerializer,
    RepDataSerializer, 
    RoutineComponentDataSerializer, 
    RoutineDataSerializer,
    PatientSerializer
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

class RepDataViewSet(viewsets.ModelViewSet):
    queryset = RepData.objects.all()
    serializer_class = RepDataSerializer

class RoutineComponentDataViewSet(viewsets.ModelViewSet):
    queryset = RoutineComponentData.objects.all()
    serializer_class = RoutineComponentDataSerializer

class RoutineDataViewSet(viewsets.ModelViewSet):
    queryset = RoutineData.objects.all()
    serializer_class = RoutineDataSerializer

    def get_serializer(self, *args, **kwargs):
        """Pass request context to serializer."""
        kwargs.setdefault('context', self.get_serializer_context())
        return super().get_serializer(*args, **kwargs)

    # def get_queryset(self):
    #     qs = super().get_queryset()

    #     return qs.select_related('routine_config').prefetch_related(
    #         Prefetch(
    #             'routine_component_data',
    #             queryset=RoutineComponentData.objects.prefetch_related(
    #                 Prefetch(
    #                     'rep_data',
    #                     queryset=RepData.objects.all(),
    #                 )
    #             )
    #         )
    #     )

    def get_queryset(self):
        # Cache key for the "GET all routineData" call
        cache_key = "all_routine_data"

        # Check if the cached data exists
        cached_data = cache.get(cache_key)
        if cached_data:
            return cached_data

        # If no cache, fetch the data from the database
        qs = super().get_queryset()
        qs = qs.select_related('routine_config').prefetch_related('routine_component_data__rep_data')

        # Cache the result for 15 minutes (or as desired)
        cache.set(cache_key, qs, timeout=9000)  # Cache timeout in seconds

        return qs

    def perform_create(self, serializer):
        # Save the object
        instance = serializer.save()

        # Invalidate the cache for all routineData
        cache.delete("all_routine_data")

    def perform_update(self, serializer):
        # Save the object
        instance = serializer.save()

        # Invalidate the cache for all routineData
        cache.delete("all_routine_data")

    def perform_destroy(self, instance):
        # Invalidate the cache for all routineData before deleting
        cache.delete("all_routine_data")

        # Delete the object
        instance.delete()

# Kinda sloppy way to do this, since we are skipping Users + Auth for patients atm.
class PatientViewSet(viewsets.ModelViewSet):
    queryset = Patient.objects.all()
    serializer_class = PatientSerializer
    # TODO:
    # permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        """
        If a user ID is provided via query params, filter by that user.
        Otherwise, return all patients (e.g., for an admin).
        """
        user_id = self.request.query_params.get("user_id")
        if user_id:
            return Patient.objects.filter(user_id=user_id)
        return Patient.objects.all()

    @action(detail=False, methods=['get'])
    def my_profile(self, request):
        """Retrieve the patient record based on the authenticated user's ID"""
        user_id = request.query_params.get("user_id")  # Expecting user_id from request
        if not user_id:
            return Response({"error": "User ID is required"}, status=400)
        
        patient = Patient.objects.filter(user_id=user_id).first()
        if not patient:
            return Response({"error": "Patient not found"}, status=404)

        serializer = self.get_serializer(patient)
        return Response(serializer.data)
