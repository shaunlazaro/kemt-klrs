API for Pose Estimator

Version:
Using Python 3.11.7 (any similar version should be fine)

Setup:
1: Recommended to use venv `python -m venv venv`
`source venv/bin/activate` (bash)
OR `venv/scripts/Activate.ps1` (windows powershell)
2: Install requirements `pip install requirements.txt`

If using local DB:
3: Run migrations `python manage.py migrate`
4: Create superuser `python manage.py createsuperuser --username admin --email admin@example.com`
5: Run dev mode server using: `python manage.py runserver 0.0.0.0:8000`
6: Access admin panel from url/admin (e.g. `127.0.0.1:8000/admin`)

Other:
    Make Migrations: `python manage.py makemigrations`
    Run Migrations: `python manage.py migrate`

Deployment:

Site is currently deployed on VM with public IP: 140.238.151.117
Access Django at: http://140.238.151.117:8000/admin
Access API spec at: http://140.238.151.117:8000/api/schema/swagger-ui/

To connect, use ssh (will need to ask me for the key).
Start server as normal, but inside of a tmux session while connected over ssh.

`tmux attach` to enter session

`ctrl+b :detach` to exit without terminating

`tmux` to start new session


API Spec:
Automatically generated using drf-spectacular.
https://github.com/tfranzel/drf-spectacular/

Generate new schema.yml file:
`python ./manage.py spectacular --color --file schema.yml`
 (Access using swagger-ui path above)