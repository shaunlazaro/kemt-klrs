from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
from poseapp.models import RoutineData

class Command(BaseCommand):
    help = "Backfill user field in RoutineData model"

    def add_arguments(self, parser):
        parser.add_argument('--username', type=str, help='Username of the default user to assign')

    def handle(self, *args, **options):
        User = get_user_model()
        username = options['username']

        if not username:
            self.stdout.write(self.style.ERROR("Please provide a username using --username"))
            return

        try:
            user = User.objects.get(username=username)
        except User.DoesNotExist:
            self.stdout.write(self.style.ERROR(f"User '{username}' not found."))
            return

        updated = 0
        for routine_data in RoutineData.objects.filter(user__isnull=True):
            routine_data.user = user
            routine_data.save()
            updated += 1

        self.stdout.write(self.style.SUCCESS(f"Updated {updated} RoutineData records to user '{username}'"))
