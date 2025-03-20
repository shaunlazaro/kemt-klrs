import { isRouteErrorResponse, useRouteError } from "react-router-dom";

const ErrorPage: React.FC = () => {
  const error = useRouteError();
  console.error(error);

  let errorMessage: string;

  if (isRouteErrorResponse(error)) {
    // error is type `ErrorResponse`
    errorMessage = error.data?.error?.message || error.statusText;
  } else if (error instanceof Error) {
    errorMessage = error.message;
  } else if (typeof error === 'string') {
    errorMessage = error;
  } else {
    console.error(error);
    errorMessage = 'Unknown error';
  }

  return (
    <div id='error-page' className='flex flex-col gap-8 justify-center items-center h-screen'>
      <h1 className='text-4xl font-bold'>Oops!</h1>
      <p>Sorry, an unexpected error has occurred.</p>
      <p className='text-slate-400'>
        <i>{errorMessage}</i>
      </p>
    </div>
  );
}

export default ErrorPage;
