document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const recoveryOtpForm =
        document.getElementById("recoveryOtpForm");

    const codeInput =
        document.getElementById("code");

    const verifyOtpButton =
        document.getElementById("verifyOtpButton");

    const errorMessage =
        document.getElementById("errorMessage");

    const successMessage =
        document.getElementById("successMessage");

    const emailDisplay =
        document.getElementById("emailDisplay");


    // =========================================================
    // GET EMAIL FROM SESSION STORAGE
    // =========================================================

    const email =
        sessionStorage.getItem("loginEmail");


    // =========================================================
    // CHECK EMAIL
    // =========================================================

    if (!email) {

        showError(
            "Recovery session not found. Please start the recovery process again."
        );


        setTimeout(function () {

            window.location.href =
                "/2fa/recovery/email";

        }, 2000);


        return;
    }


    // =========================================================
    // SHOW EMAIL
    // =========================================================

    if (emailDisplay) {

        emailDisplay.textContent =
            email;
    }


    // =========================================================
    // OTP INPUT
    // =========================================================

    codeInput.addEventListener(
        "input",
        function () {

            // Only digits
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

    recoveryOtpForm.addEventListener(
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
                    "Please enter a valid 6-digit email OTP."
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
                // VERIFY EMAIL OTP
                // =============================================
                //
                // POST:
                //
                // /auth/2fa/recovery/verify-email?email=...
                //
                // Body:
                //
                // {
                //     "code": "123456"
                // }
                //
                // Browser automatically sends
                // twoFactorLoginToken cookie.
                // =============================================

                const response =
                    await fetch(
                        "/auth/2fa/recovery/verify-email?email=" +
                        encodeURIComponent(email),
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
                        "Unable to parse recovery OTP response:",
                        error
                    );
                }


                // =============================================
                // FAILED
                // =============================================

                if (!response.ok) {

                    showError(
                        data.message ||
                        "Invalid or expired email OTP."
                    );

                    codeInput.value = "";

                    codeInput.focus();

                    return;
                }


                // =============================================
                // CHECK QR CODE
                // =============================================
                //
                // Backend successful email verification ke
                // baad NEW Google Authenticator QR return karega.
                // =============================================

                if (!data.qrCode) {

                    showError(
                        "New authenticator QR code was not received from server."
                    );

                    return;
                }


                // =============================================
                // STORE NEW QR URL
                // =============================================
                //
                // recovery-2fa.js ise read karega.
                // =============================================

                sessionStorage.setItem(
                    "qrCodeUrl",
                    data.qrCode
                );


                // =============================================
                // KEEP EMAIL
                // =============================================

                sessionStorage.setItem(
                    "loginEmail",
                    email
                );


                // =============================================
                // SUCCESS MESSAGE
                // =============================================

                showSuccess(
                    data.message ||
                    "Email verified successfully. Setting up your new authenticator..."
                );


                // =============================================
                // REDIRECT TO NEW 2FA SETUP
                // =============================================

                setTimeout(function () {

                    window.location.href =
                        "/2fa/recovery/setup";

                }, 700);


            } catch (error) {

                console.error(
                    "Recovery email OTP verification failed:",
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

        if (!verifyOtpButton) {
            return;
        }


        verifyOtpButton.disabled =
            isLoading;


        if (isLoading) {

            verifyOtpButton.innerHTML = `
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true">
                </span>
                Verifying...
            `;

        } else {

            verifyOtpButton.innerHTML =
                "Verify Email OTP";
        }
    }

});