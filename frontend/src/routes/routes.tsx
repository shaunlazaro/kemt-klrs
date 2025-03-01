import { createBrowserRouter, createRoutesFromElements, Navigate, Outlet, Route } from 'react-router-dom';
import { ErrorPage, Exercises, Home, Patients, Signin } from '../pages';
import { PageTemplate } from '../page.template';
import { ADDEDIT_EXERCISES_PATH, EXERCISES_PATH, HOME_PATH, PATIENTS_PATH } from './name';
import AddEditPlan from '../pages/exercises/addEditPlan';

const ProtectedRoute: React.FC = () => {
  // const session = useSession();

  // if (!session) {
  //   return <Navigate to={SIGNIN_PATH} replace />;
  // }

  return (
    <PageTemplate>
      <Outlet />
    </PageTemplate>
  );
};


const routes = createRoutesFromElements(
  <Route element={<ProtectedRoute />} errorElement={<ErrorPage />}>
    <Route path={HOME_PATH} element={<Home />} />
    <Route path="/signin" element={<Signin />} />
    {/* <Route path="/register" element={<Register />} /> */}
    <Route path={PATIENTS_PATH} element={<Patients />} />
    <Route path={EXERCISES_PATH} element={<Exercises />} />
    <Route path={ADDEDIT_EXERCISES_PATH} element={<AddEditPlan />} />
  </Route>
);

export default createBrowserRouter(routes);