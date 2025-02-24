import { createBrowserRouter, createRoutesFromElements, Navigate, Outlet, Route } from 'react-router-dom';
import { ErrorPage, Exercises, Home, Patients, Signin} from '../pages';
import { PageTemplate } from '../page.template';

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
  <Route element={<ProtectedRoute/> } errorElement={<ErrorPage />}>
    <Route path="/" element={<Home />} />
    <Route path="/signin" element={<Signin />} />
    {/* <Route path="/register" element={<Register />} /> */}
    <Route path="/patients" element={<Patients />} />
    <Route path="/exercises" element={<Exercises />} />
  </Route>
);

export default createBrowserRouter(routes);