// TODO: Move these addresses to .env

import { apiBase } from "../../api/request";

// const GOOGLE_CLIENT_ID = "<your-google-client-id>.apps.googleusercontent.com";
const GOOGLE_CLIENT_ID = "825627223283-7he30709s5prqngspqcskquq4bf8ud4t.apps.googleusercontent.com"
const REDIRECT_URI = `${apiBase}/auth/google/callback/`;

// Signin flow:
// 1: Click signin with google
// 2: Redirect to google signin page with RePose client id
// 3: User signs in
// 4: Google redirects to the redirect callback (our server) with some id
// 5: Server takes the id for auth, and verifies identity, then returns a token
// 6: We get sent to login-success page with token
// 7: Store token in local storage, as the user's id
export const GoogleLoginButton = () => {
    const handleLogin = () => {
        const baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
        const params = new URLSearchParams({
            client_id: GOOGLE_CLIENT_ID,
            redirect_uri: REDIRECT_URI,
            response_type: "code",
            scope: "openid email profile",
            prompt: "consent",
        });

        window.location.href = `${baseUrl}?${params.toString()}`;
    };

    return (<img src="signin_button.png" onClick={handleLogin} />)
};