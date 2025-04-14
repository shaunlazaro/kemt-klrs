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
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.authtoken.models import Token
from rest_framework.permissions import AllowAny, IsAuthenticated
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
            print(f"grabbing from cache: {cache_key}")
            response = cache.get(cache_key)
            if response:
                return response

            response = view_func(self, request, *args, **kwargs)
            
            # Defer caching until the response is fully rendered
            def cache_after_render(r):
                print(f"caching: {cache_key}")
                patch_response_headers(r, timeout)
                cache.set(cache_key, r, timeout)
            response.add_post_render_callback(cache_after_render)
            return response
        return _wrapped_view
    return decorator

# TODO: Move to some cache specific script.
def invalidate_user_list_cache(request):
    user = request.user
    if not user.is_authenticated:
        return
    cache_key = f"user_cache:{user.id}:{request.get_full_path()}"
    print(f"clear cache: {cache_key}")
    cache.delete(cache_key)
    cache_key = f"user_cache:{user.id}:{request.get_full_path()}app/"
    print(f"clear cache: {cache_key}")
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
        qs = qs.select_related('routine_config') \
               .prefetch_related('routine_component_data__rep_data')
        return qs

    @user_cache_page(60 * 15)
    def list(self, request, *args, **kwargs):
        return super().list(request, *args, **kwargs)

    @action(detail=False, methods=['get'], url_path='app', permission_classes=[IsAuthenticated])
    @user_cache_page(60 * 15)
    def app_list(self, request, *args, **kwargs):
        # Custom action to filter routine data by current user
        qs = self.get_queryset().filter(user=request.user)
        serializer = self.get_serializer(qs, many=True)
        return Response(serializer.data)

    @action(detail=False, methods=['get'], url_path=r'patient/(?P<patient_id>\d+)', permission_classes=[IsAuthenticated])
    @user_cache_page(60 * 15)
    def patient_routine_data(self, request, patient_id=None, *args, **kwargs):
        # Filter routine data by patient's user
        try:
            patient = Patient.objects.get(id=patient_id)
        except Patient.DoesNotExist:
            return Response({"detail": "Patient not found."}, status=404)

        # Retrieve routine data where user matches the patient
        qs = self.get_queryset().filter(user=patient.user)
        serializer = self.get_serializer(qs, many=True)
        return Response(serializer.data)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
        invalidate_user_list_cache(self.request)

    def perform_update(self, serializer):
        serializer.save()
        invalidate_user_list_cache(self.request)

    def perform_destroy(self, instance):
        instance.delete()
        invalidate_user_list_cache(self.request)

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
@permission_classes([AllowAny])
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
        print("Token exchange failed:", token_res.status_code, token_res.text)
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
    first_name = userinfo.get('given_name', '')
    last_name = userinfo.get('family_name', '')

    # Create or get user
    user, created = User.objects.get_or_create(
        email=email,
        defaults={
            "username": email,
            "first_name": first_name,
            "last_name": last_name,
        }
    )

    # If the user was just created, also create a corresponding Patient
    if created:
        # Create a Patient associated with the new user
        Patient.objects.create(
            user=user,
            first_name=first_name,
            last_name=last_name,
            email=email,
            sex='O',
            condition='Not Specified', 
        )

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


# -------------
# DASHBOARD:
# -------------
from .models import Patient, RoutineData, RoutineExercise

class PatientDashboardView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        results = []

        patients = Patient.objects.select_related('user', 'exercises').all()

        for patient in patients:
            routine_datas = RoutineData.objects.filter(user=patient.user).prefetch_related(
                'routine_component_data__rep_data', 'routine_component_data__exercise_detail', 'routine_config__exercises'
            )

            # Default values
            avg_score = 0.0
            avg_completion = 0.0
            avg_rating = 0.0

            if routine_datas.exists():
                total_routine_scores = []
                total_completion = []
                total_ratings = []

                for routine_data in routine_datas:
                    component_data_list = routine_data.routine_component_data.all()

                    routine_scores = []
                    completion_percents = []
                    ratings = []

                    for component in component_data_list:
                        rep_data_qs = component.rep_data.all()
                        max_scores = [rep.max_score for rep in rep_data_qs]
                        if max_scores:
                            avg_component_score = sum(max_scores) / len(max_scores)
                            routine_scores.append(avg_component_score)

                        expected_reps = RoutineExercise.objects.filter(
                            routine=routine_data.routine_config,
                            exercise=component.exercise_detail
                        ).first()
                        expected_rep_count = expected_reps.reps if expected_reps and expected_reps.reps is not None else 0

                        if expected_rep_count > 0:
                            completion = len(rep_data_qs) / expected_rep_count
                            completion_percents.append(completion)

                        ratings.append(component.rating)

                    if routine_scores:
                        total_routine_scores.append(sum(routine_scores) / len(routine_scores))
                    if completion_percents:
                        total_completion.append(sum(completion_percents) / len(completion_percents))
                    if ratings:
                        total_ratings.extend(ratings)

                avg_score = sum(total_routine_scores) / len(total_routine_scores) if total_routine_scores else 0.0
                avg_completion = sum(total_completion) / len(total_completion) if total_completion else 0.0
                avg_rating = sum(total_ratings) / len(total_ratings) if total_ratings else 0.0

            results.append({
                'patient': str(patient),
                'average_score': round(avg_score, 2),
                'completion_percent': round(avg_completion * 100, 2),
                'average_rating': round(avg_rating, 2),
            })

        return Response(results)
