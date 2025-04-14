from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
from poseapp.models import Patient

class Command(BaseCommand):
    help = "Backfill user field in Patient model based on matching email"

    def handle(self, *args, **options):
        User = get_user_model()
        matched = 0
        unmatched = 0

        for patient in Patient.objects.filter(user__isnull=True):
            try:
                user = User.objects.get(email=patient.email)
                patient.user = user
                patient.save()
                matched += 1
            except User.DoesNotExist:
                unmatched += 1
                self.stdout.write(self.style.WARNING(
                    f"No matching user found for patient: {patient.email}"
                ))

        self.stdout.write(self.style.SUCCESS(f"Backfill complete. Matched: {matched}, Unmatched: {unmatched}"))
