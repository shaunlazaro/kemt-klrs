import requests
from django.conf import settings
from django.shortcuts import render, redirect
from django.contrib.auth.models import User
from django.db.models import Prefetch
from django.core.cache import cache
from django.middleware.cache import CacheMiddleware
from django.utils.cache import patch_response_headers
from django.utils.decorators import method_decorator, decorator_from_middleware_with_args
from django.views.decorators.cache import cache_page
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.authtoken.models import Token
from rest_framework.permissions import AllowAny
from functools import wraps
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

def user_cache_page(timeout):
    def decorator(view_func):
        @wraps(view_func)
        def _wrapped_view(self, request, *args, **kwargs):
            user = request.user
            if not user.is_authenticated:
                return view_func(self, request, *args, **kwargs)

            cache_key = f"user_cache:{user.id}:{request.get_full_path()}"
            response = cache.get(cache_key)
            if response:
                return response

            response = view_func(self, request, *args, **kwargs)
            response.render()
            cache.set(cache_key, response, timeout)
            patch_response_headers(response, timeout)
            return response
        return _wrapped_view
    return decorator

# TODO: Move to some cache specific script.
def invalidate_user_list_cache(request):
    user = request.user
    if not user.is_authenticated:
        return
    cache_key = f"user_cache:{user.id}:{request.get_full_path()}"
    cache.delete(cache_key)

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

    # @method_decorator(cache_page(60 * 15, key_prefix="routine_data_list"), name="list")  # cache for 15 minutes
    # def list(self, request, *args, **kwargs):
    #     return super().list(request, *args, **kwargs)

    @user_cache_page(60 * 15)
    def list(self, request, *args, **kwargs):
        return super().list(request, *args, **kwargs)

    def create(self, request, *args, **kwargs):
        response = super().create(request, *args, **kwargs)
        invalidate_user_list_cache(request)
        return response

    def update(self, request, *args, **kwargs):
        response = super().update(request, *args, **kwargs)
        invalidate_user_list_cache(request)
        return response

    def destroy(self, request, *args, **kwargs):
        response = super().destroy(request, *args, **kwargs)
        invalidate_user_list_cache(request)
        return response

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
def google_callback_android(request):
    code = request.query_params.get('code')

    if not code:
        return Response({'error': 'No code provided'}, status=400)

    # Exchange code for tokens
    token_res = requests.post(GOOGLE_TOKEN_URL, data={
        'code': code,
        'client_id': settings.GOOGLE_ANDROID_CLIENT_ID,
        'client_secret': settings.GOOGLE_ANDROID_CLIENT_SECRET,
        'redirect_uri': settings.GOOGLE_ANDROID_REDIRECT_URI,
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

@permission_classes([AllowAny])
def google_callback_web(request):
    code = request.GET.get("code")
    print("gcallbackweb")
    if not code:
        return redirect(f"{settings.FRONTEND_URL}/login-failed")
    print(f"code: {code}")
    # Exchange code for tokens
    token_data = {
        "code": code,
        "client_id": settings.GOOGLE_CLIENT_ID,
        "client_secret": settings.GOOGLE_CLIENT_SECRET,
        "redirect_uri": settings.GOOGLE_REDIRECT_URI,
        "grant_type": "authorization_code",
    }
    token_resp = requests.post(GOOGLE_TOKEN_URL, data=token_data)
    token_json = token_resp.json()
    access_token = token_json.get("access_token")
    print(f"atoken: {token_resp}")
    if not access_token:
        return redirect(f"{settings.FRONTEND_URL}/login-failed")

    # Get user info
    userinfo_resp = requests.get(
        GOOGLE_USERINFO_URL,
        headers={"Authorization": f"Bearer {access_token}"}
    )
    userinfo = userinfo_resp.json()
    email = userinfo.get("email")

    if not email:
        return redirect(f"{settings.FRONTEND_URL}/login-failed")

    user, _ = User.objects.get_or_create(
        email=email,
        defaults={
            "username": email,
            "first_name": userinfo.get("given_name", ""),
            "last_name": userinfo.get("family_name", ""),
        }
    )
    # Create or get DRF token
    token, _ = Token.objects.get_or_create(user=user)

    # Redirect to frontend with token
    return redirect(f"{settings.FRONTEND_URL}/login-success?token={token.key}")