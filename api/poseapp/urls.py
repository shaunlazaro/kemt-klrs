from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import PoseViewSet, RoutineViewSet

# Initialize the router and register your viewsets
router = DefaultRouter()
router.register(r'poses', PoseViewSet)
router.register(r'routines', RoutineViewSet)

# Define the app's URL patterns
urlpatterns = [
    path('', include(router.urls)),  # Include the router's URLs
]