import { createBrowserRouter, createRoutesFromElements, Navigate, Outlet, Route } from 'react-router-dom';
import { ErrorPage, Exercises, Home, Patients, Signin } from '../pages';
import { PageTemplate, LoginPageTemplate } from '../page.template';
import { ADDEDIT_EXERCISES_PATH, ADDEDIT_PATIENTS_PATH, EXERCISES_PATH, HOME_PATH, PATIENT_REPORT_PATH, PATIENT_REPORT_SINGLE_PATH, PATIENTS_PATH, SIGNIN_FAILED_PATH, SIGNIN_PATH, SIGNIN_SUCCESS_PATH } from './name';
import AddEditPlan from '../pages/exercises/addEditPlan';
import AddEditPatients from '../pages/patients/addEditPatients';
import PatientReport from '../pages/patients/patientReport';
import PatientSingleWorkoutReport from '../pages/patients/patientSingleWorkoutReport';
import { useSession } from '../api/useSession';
import SigninFail from '../pages/signin/SigninFail';
import SigninSuccess from '../pages/signin/SigninSuccess';

const ProtectedRoute: React.FC = () => {
  // const session = useSession();

  // if (!session) {
  //   return <Navigate to={SIGNIN_PATH} replace />;
  // }
  const session = useSession();

  if (!session) {
    return <Navigate to={SIGNIN_PATH} replace />;
  }

  return (
    <PageTemplate>
      <Outlet />
    </PageTemplate>
  );
};
const AuthRoute: React.FC = () => {
  return (
    <LoginPageTemplate>
      <Outlet />
    </LoginPageTemplate>
  );
};


const routes = createRoutesFromElements(
  [
    <Route element={<ProtectedRoute />} errorElement={<ErrorPage />}>
      <Route path={HOME_PATH} element={<Home />} />
      {/* <Route path="/register" element={<Register />} /> */}
      <Route path={PATIENTS_PATH} element={<Patients />} />
      <Route path={ADDEDIT_PATIENTS_PATH} element={<AddEditPatients />} />
      <Route path={PATIENT_REPORT_PATH} element={<PatientReport />} />
      <Route path={PATIENT_REPORT_SINGLE_PATH} element={<PatientSingleWorkoutReport />} />
      <Route path={EXERCISES_PATH} element={<Exercises />} />
      <Route path={ADDEDIT_EXERCISES_PATH} element={<AddEditPlan />} />
    </Route>,
    <Route element={<AuthRoute />} errorElement={<ErrorPage />}>
      <Route path={SIGNIN_PATH} element={<Signin />} />
      <Route path={SIGNIN_FAILED_PATH} element={<SigninFail />} />
      <Route path={SIGNIN_SUCCESS_PATH} element={<SigninSuccess />} />
      {/* <Route path="/register" element={<Register />} /> */}
    </Route>
  ]
);

export default createBrowserRouter(routes);