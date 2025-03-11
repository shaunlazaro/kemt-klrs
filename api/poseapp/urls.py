from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    PoseViewSet, 
    RoutineViewSet,
    TrackingDetailViewSet,
    ExerciseDetailViewSet,
    RoutineConfigViewSet,
    RoutineExerciseViewSet,
    RepDataViewSet,
    RoutineComponentDataViewSet,
    RoutineDataViewSet,
)

# Initialize the router and register your viewsets
router = DefaultRouter()
router.register(r'poses', PoseViewSet)
router.register(r'routines', RoutineViewSet)
router.register(r'tracking-details', TrackingDetailViewSet)
router.register(r'exercise-details', ExerciseDetailViewSet)
router.register(r'routine-configs', RoutineConfigViewSet)
router.register(r'routine-exercises', RoutineExerciseViewSet)
router.register(r'rep-data', RepDataViewSet)
router.register(r'routine-component-data', RoutineComponentDataViewSet)
router.register(r'routine-data', RoutineDataViewSet)

# Define the app's URL patterns
urlpatterns = [
    path('', include(router.urls)),  # Include the router's URLs
]