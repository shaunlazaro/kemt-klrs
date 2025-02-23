import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router-dom';
import { ErrorPage, Exercises, Home, Patients, Signin} from '../pages';

const routes = createRoutesFromElements(
  <Route errorElement={<ErrorPage />}>
    <Route path="/" element={<Home />} />
    <Route path="/signin" element={<Signin />} />
    {/* <Route path="/register" element={<Register />} /> */}
    <Route path="/patients" element={<Patients />} />
    <Route path="/exercises" element={<Exercises />} />
  </Route>
);

export default createBrowserRouter(routes);