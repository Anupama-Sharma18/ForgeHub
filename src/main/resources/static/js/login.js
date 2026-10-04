document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const loginForm = document.getElementById("loginForm");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");

    const loginButton = document.getElementById("loginButton");

    const errorMessage = document.getElementById("errorMessage");

    // =========================================================
    // LOGIN FORM SUBMIT
    // =========================================================

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();


        // -----------------------------------------------------
        // CLEAR PREVIOUS MESSAGES
        // -----------------------------------------------------

        hideMessage(errorMessage);



        // -----------------------------------------------------
        // GET FORM VALUES
        // -----------------------------------------------------

        const email = emailInput.value.trim();
        const password = passwordInput.value;


        // -----------------------------------------------------
        // CLIENT-SIDE VALIDATION
        // -----------------------------------------------------

        if (email === "") {

            showError("Please enter your email.");

            emailInput.focus();

            return;
        }


        if (!isValidEmail(email)) {

            showError("Please enter a valid email address.");

            emailInput.focus();

            return;
        }


        if (password === "") {

            showError("Please enter your password.");

            passwordInput.focus();

            return;
        }




        // =====================================================
        // DISABLE LOGIN BUTTON
        // =====================================================

        setLoading(true);


        try {

            // =================================================
            // CALL BACKEND LOGIN API
            // =================================================

            const response = await fetch(
                "/auth/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    credentials: "same-origin",

                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                }
            );


            // =================================================
            // READ RESPONSE
            // =================================================

            let data = {};

            try {

                data = await response.json();

            } catch (error) {

                console.error(
                    "Unable to parse login response:",
                    error
                );
            }


            // =================================================
            // LOGIN FAILED
            // =================================================

            if (!response.ok) {

                const message =
                    data.message ||
                    "Invalid email or password.";

                showError(message);

                return;
            }


            // =================================================
            // FIRST-TIME LOGIN
            // =================================================
            //
            // Backend response:
            //
            // requiresTwoFactorSetup = true
            // qrCode = QR URL
            //
            // Backend has already created:
            // twoFactorSetupToken HttpOnly cookie
            //
            // No access token yet.
            // No refresh token yet.
            //
            // Next page = /2fa/setup
            // =================================================

            if (
                data.requiresTwoFactorSetup === true
            ) {

                // ---------------------------------------------
                // Store QR URL temporarily
                // ---------------------------------------------
                //
                // JavaScript cannot read the HttpOnly
                // twoFactorSetupToken cookie.
                //
                // QR URL is only needed by the next page.
                // ---------------------------------------------

                if (data.qrCode) {

                    sessionStorage.setItem(
                        "qrCodeUrl",
                        data.qrCode
                    );

                } else {

                    showError(
                        "2FA QR code was not received from server."
                    );

                    return;
                }


                // ---------------------------------------------
                // Store login email
                // ---------------------------------------------

                sessionStorage.setItem(
                    "loginEmail",
                    email
                );


                window.location.href = "/2fa/setup";


                return;
            }


            // =================================================
            // NORMAL / EXISTING USER LOGIN
            // =================================================
            //
            // Backend response:
            //
            // requiresTwoFactorSetup = false
            //
            // Backend creates:
            // twoFactorLoginToken HttpOnly cookie
            //
            // No access token yet.
            // User must enter Google Authenticator OTP.
            //
            // Next page = /2fa
            // =================================================

            sessionStorage.setItem(
                "loginEmail",
                email
            );

//redirect to 2fa page
            window.location.href = "/2fa";


        } catch (error) {

            console.error(
                "Login request failed:",
                error
            );


            showError(
                "Unable to connect to the server. Please try again."
            );

        } finally {

            setLoading(false);
        }

    });


    // =========================================================
    // EMAIL VALIDATION
    // =========================================================

    function isValidEmail(email) {

        const emailPattern =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        return emailPattern.test(email);
    }


    // =========================================================
    // SHOW ERROR
    // =========================================================

    function showError(message) {

        if (!errorMessage) {
            return;
        }

        errorMessage.textContent = message;

        errorMessage.classList.remove(
            "d-none"
        );
    }



    // =========================================================
    // HIDE MESSAGE
    // =========================================================

    function hideMessage(element) {

        if (!element) {
            return;
        }

        element.classList.add(
            "d-none"
        );

        element.textContent = "";
    }


    // =========================================================
    // LOADING STATE
    // =========================================================

    function setLoading(isLoading) {

        if (!loginButton) {
            return;
        }


        loginButton.disabled =
            isLoading;


        if (isLoading) {

            loginButton.innerHTML = `
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true">
                </span>
                Logging in...
            `;

        } else {

            loginButton.innerHTML =
                "Login";
        }
    }

});