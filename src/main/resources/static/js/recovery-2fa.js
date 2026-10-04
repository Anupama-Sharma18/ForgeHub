document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const recoveryTwoFactorForm =
        document.getElementById("recoveryTwoFactorForm");

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

    if (email && loginEmail) {

        loginEmail.textContent =
            email;
    }


    // =========================================================
    // CHECK QR CODE
    // =========================================================

    if (!qrCodeUrl) {

        showError(
            "QR code information is missing or expired. Please restart the recovery process."
        );


        setTimeout(function () {

            window.location.href =
                "/2fa/recovery/email";

        }, 2000);


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

            // Remove anything except digits
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

    recoveryTwoFactorForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            // -------------------------------------------------
            // CLEAR MESSAGES
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
            // LOADING
            // -------------------------------------------------

            setLoading(true);


            try {

                // =============================================
                // CALL BACKEND
                // =============================================
                //
                // Important:
                //
                // JavaScript does NOT send the
                // twoFactorRecoveryToken manually.
                //
                // It is HttpOnly.
                //
                // Browser automatically sends the cookie.
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
                        "Unable to parse response:",
                        error
                    );
                }


                // =============================================
                // FAILED
                // =============================================

                if (!response.ok) {

                    showError(
                        data.message ||
                        "Invalid or expired Google Authenticator code."
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
                // SAVE ACCESS TOKEN
                // =============================================
                //
                // Refresh token is NOT stored here.
                //
                // Backend stores refresh token in
                // HttpOnly cookie.
                // =============================================

                sessionStorage.setItem(
                    "accessToken",
                    data.accessToken
                );


                // =============================================
                // REMOVE TEMPORARY QR DATA
                // =============================================

                sessionStorage.removeItem(
                    "qrCodeUrl"
                );


                // =============================================
                // SUCCESS
                // =============================================

                showSuccess(
                    "2FA recovery completed successfully. Redirecting..."
                );


                // =============================================
                // REDIRECT TO DASHBOARD
                // =============================================

                setTimeout(function () {

                    window.location.href =
                        "/admin/dashboard";

                }, 700);


            } catch (error) {

                console.error(
                    "Recovery 2FA verification failed:",
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
    // BUTTON LOADING
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
                "Verify & Continue";
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