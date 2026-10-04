document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const setupTwoFactorForm =
        document.getElementById("setupTwoFactorForm");

    const codeInput =
        document.getElementById("code");

    const verifyButton =
        document.getElementById("verifyButton");

    const qrCodeContainer =
        document.getElementById("qrCode");

    const errorMessage =
        document.getElementById("errorMessage");

    const successMessage =
        document.getElementById("successMessage");

    const loginEmail =
        document.getElementById("loginEmail");


    // =========================================================
    // GET DATA FROM SESSION STORAGE
    // =========================================================

    const email =
        sessionStorage.getItem("loginEmail");

    const qrCodeUrl =
        sessionStorage.getItem("qrCodeUrl");


    // =========================================================
    // CHECK EMAIL
    // =========================================================

    if (!email) {

        showError(
            "Login session not found. Please login again."
        );


        setTimeout(function () {

            window.location.href =
                "/login";

        }, 2000);


        return;
    }


    // =========================================================
    // SHOW EMAIL
    // =========================================================

    if (loginEmail) {

        loginEmail.textContent =
            email;
    }


    // =========================================================
    // CHECK QR URL
    // =========================================================

    if (!qrCodeUrl) {

        showError(
            "2FA QR code is missing or expired. Please login again."
        );


        setTimeout(function () {

            window.location.href =
                "/login";

        }, 2500);


        return;
    }


    // =========================================================
    // GENERATE QR CODE
    // =========================================================

    try {

        new QRCode(
            qrCodeContainer,
            {
                text: qrCodeUrl,

                width: 220,

                height: 220,

                correctLevel:
                    QRCode.CorrectLevel.M
            }
        );


    } catch (error) {

        console.error(
            "QR code generation failed:",
            error
        );


        showError(
            "Unable to generate QR code."
        );


        return;
    }


    // =========================================================
    // OTP INPUT
    // =========================================================

    codeInput.addEventListener(
        "input",
        function () {

            // Only numbers
            this.value =
                this.value.replace(/\D/g, "");


            // Maximum 6 digits
            if (this.value.length > 6) {

                this.value =
                    this.value.substring(0, 6);
            }
        }
    );


    // =========================================================
    // FORM SUBMIT
    // =========================================================

    setupTwoFactorForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            // -------------------------------------------------
            // CLEAR OLD MESSAGES
            // -------------------------------------------------

            hideMessage(errorMessage);

            hideMessage(successMessage);


            // -------------------------------------------------
            // GET OTP
            // -------------------------------------------------

            const code =
                codeInput.value.trim();


            // -------------------------------------------------
            // VALIDATE OTP
            // -------------------------------------------------

            if (!/^\d{6}$/.test(code)) {

                showError(
                    "Please enter a valid 6-digit Google Authenticator code."
                );

                codeInput.focus();

                return;
            }


            // -------------------------------------------------
            // LOADING STATE
            // -------------------------------------------------

            setLoading(true);


            try {

                // =============================================
                // VERIFY GOOGLE AUTHENTICATOR OTP
                // =============================================
                //
                // Backend:
                //
                // POST /auth/2fa/verify
                //
                // Body:
                //
                // {
                //      "code": "123456"
                // }
                //
                // twoFactorSetupToken is HttpOnly.
                // Browser automatically sends it.
                // =============================================

                const response =
                    await fetch(
                        "/auth/2fa/verify",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            credentials:
                                "same-origin",

                            body: JSON.stringify({

                                code: code

                            })
                        }
                    );


                // =============================================
                // READ RESPONSE
                // =============================================

                let data = {};

                try {

                    data =
                        await response.json();

                } catch (error) {

                    console.error(
                        "Unable to parse 2FA response:",
                        error
                    );
                }


                // =============================================
                // VERIFICATION FAILED
                // =============================================

                if (!response.ok) {

                    showError(
                        data.message ||
                        "Invalid or expired authentication code."
                    );


                    codeInput.value = "";

                    codeInput.focus();

                    return;
                }


                // =============================================
                // CHECK ACCESS TOKEN
                // =============================================

                if (!data.accessToken) {

                    showError(
                        "Access token was not received from server."
                    );

                    return;
                }


                // =============================================
                // STORE ACCESS TOKEN
                // =============================================
                //
                // Access token will be used for protected
                // API requests.
                //
                // Refresh token is NOT stored in JavaScript.
                // Backend stores it in HttpOnly cookie.
                // =============================================

                sessionStorage.setItem(
                    "accessToken",
                    data.accessToken
                );


                // =============================================
                // REMOVE QR DATA
                // =============================================

                sessionStorage.removeItem(
                    "qrCodeUrl"
                );


                // =============================================
                // SHOW SUCCESS
                // =============================================

                showSuccess(
                    "2FA setup completed successfully. Redirecting..."
                );


                // =============================================
                // REDIRECT TO DASHBOARD
                // =============================================

                setTimeout(function () {

                    window.location.href =
                        "/admin/dashboard";

                }, 800);


            } catch (error) {

                console.error(
                    "2FA setup verification failed:",
                    error
                );


                showError(
                    "Unable to connect to the server. Please try again."
                );


            } finally {

                setLoading(false);
            }

        }
    );


    // =========================================================
    // SHOW ERROR
    // =========================================================

    function showError(message) {

        if (!errorMessage) {
            return;
        }

        errorMessage.textContent =
            message;

        errorMessage.classList.remove(
            "d-none"
        );
    }


    // =========================================================
    // SHOW SUCCESS
    // =========================================================

    function showSuccess(message) {

        if (!successMessage) {
            return;
        }

        successMessage.textContent =
            message;

        successMessage.classList.remove(
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

        element.textContent = "";

        element.classList.add(
            "d-none"
        );
    }


    // =========================================================
    // LOADING STATE
    // =========================================================

    function setLoading(isLoading) {

        if (!verifyButton) {
            return;
        }


        verifyButton.disabled =
            isLoading;


        if (isLoading) {

            verifyButton.innerHTML = `
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true">
                </span>
                Verifying...
            `;

        } else {

            verifyButton.innerHTML =
                "Verify & Complete Setup";
        }
    }

});
function redirectByRole(accessToken) {

    try {

        const payload =
            accessToken.split(".")[1];

        const decodedPayload =
            JSON.parse(
                atob(
                    payload
                        .replace(/-/g, "+")
                        .replace(/_/g, "/")
                )
            );

        const role =
            decodedPayload.role;


        if (role === "ADMIN") {

            window.location.href =
                "/admin/dashboard";

            return;
        }


        if (role === "VENDOR") {

            window.location.href =
                "/vendor/dashboard";

            return;
        }


        showError(
            "Invalid user role."
        );


    } catch (error) {

        console.error(
            "Unable to determine user role:",
            error
        );

        showError(
            "Unable to determine user role."
        );
    }
}