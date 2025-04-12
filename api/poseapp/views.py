import requests
from django.conf import settings
from django.shortcuts import render
from django.contrib.auth.models import User
from django.db.models import Prefetch
from django.core.cache import cache
from django.utils.decorators import method_decorator
from django.views.decorators.cache import cache_page
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.decorators import action, api_view
from rest_framework.authtoken.models import Token
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

    def get_queryset(self):
        qs = super().get_queryset()
        qs = qs.select_related('routine_config').prefetch_related('routine_component_data__rep_data')
        return qs

    @method_decorator(cache_page(60 * 15))  # cache for 15 minutes
    def list(self, request, *args, **kwargs):
        return super().list(request, *args, **kwargs)

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

# --------------
# AUTH: We'll use only Google OAuth for this prototype.
# --------------
GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"

@api_view(['GET'])
def google_callback(request):
    code = request.query_params.get('code')

    if not code:
        return Response({'error': 'No code provided'}, status=400)

    # Exchange code for tokens
    token_res = requests.post(GOOGLE_TOKEN_URL, data={
        'code': code,
        'client_id': settings.GOOGLE_CLIENT_ID,
        'client_secret': settings.GOOGLE_CLIENT_SECRET,
        'redirect_uri': settings.GOOGLE_REDIRECT_URI,
        'grant_type': 'authorization_code',
    })

    if token_res.status_code != 200:
        return Response({'error': 'Failed to get tokens'}, status=400)

    token_data = token_res.json()
    access_token = token_data.get('access_token')

    # Use access token to get user info
    userinfo_res = requests.get(GOOGLE_USERINFO_URL, headers={
        'Authorization': f'Bearer {access_token}'
    })

    if userinfo_res.status_code != 200:
        return Response({'error': 'Failed to fetch user info'}, status=400)

    userinfo = userinfo_res.json()
    email = userinfo.get('email')
    name = userinfo.get('name')

    # Create or get user
    user, _ = User.objects.get_or_create(username=email, defaults={'first_name': name, 'email': email})

    # Generate DRF token
    token, _ = Token.objects.get_or_create(user=user)

    # Return token to frontend
    return Response({'token': token.key})