document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const twoFactorForm =
        document.getElementById("twoFactorForm");

    const codeInput =
        document.getElementById("code");

    const verifyButton =
        document.getElementById("verifyButton");

    const errorMessage =
        document.getElementById("errorMessage");

    const emailDisplay =
        document.getElementById("loginEmail");


    // =========================================================
    // DISPLAY LOGIN EMAIL
    // =========================================================

    const loginEmail =
        sessionStorage.getItem("loginEmail");

    if (loginEmail && emailDisplay) {

        emailDisplay.textContent =
            loginEmail;
    }


    // =========================================================
    // CHECK LOGIN SESSION
    // =========================================================

    if (!loginEmail) {

        showError(
            "Login session not found. Please login again."
        );

        setTimeout(function () {

            window.location.href =
                "/login";

        }, 1500);

        return;
    }


    // =========================================================
    // OTP INPUT - ONLY 6 DIGITS
    // =========================================================

    codeInput.addEventListener(
        "input",
        function () {

            // Remove non-numeric characters
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

    twoFactorForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            // -------------------------------------------------
            // CLEAR OLD MESSAGES
            // -------------------------------------------------

            hideMessage(errorMessage);

            // GET OTP

            const code =
                codeInput.value.trim();


            // -------------------------------------------------
            // CLIENT SIDE VALIDATION
            // -------------------------------------------------

            if (!/^\d{6}$/.test(code)) {

                showError(
                    "Please enter a valid 6-digit Google Authenticator code."
                );

                codeInput.focus();

                return;
            }


            // -------------------------------------------------
            // BUTTON LOADING
            // -------------------------------------------------

            setLoading(true);


            try {

                // =============================================
                // CALL BACKEND
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
                        "Invalid or expired OTP."
                    );

                    codeInput.value = "";

                    codeInput.focus();

                    return;
                }


                // =============================================
                // GET ACCESS TOKEN
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
                // JWT access token browser side use hoga
                // protected API requests ke Authorization
                // header ke liye.
                //
                // Refresh token JS ko nahi milega because
                // it is stored in HttpOnly cookie.
                // =============================================

                sessionStorage.setItem(
                    "accessToken",
                    data.accessToken
                );


                // =============================================
                // CLEAN TEMPORARY DATA

                sessionStorage.removeItem(
                    "qrCodeUrl"
                );

                window.location.href = "/admin/dashboard";


            } catch (error) {

                console.error(
                    "2FA verification request failed:",
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
    // LOST YOUR OTP
    // =========================================================
    //
    // Normal login flow me user Google Authenticator OTP
    // nahi de pa raha hai.
    //
    // Recovery page par jayega.
    // =========================================================

    const lostOtpLink =
        document.getElementById("lostOtpLink");


    if (lostOtpLink) {

        lostOtpLink.addEventListener(
            "click",
            function (event) {

                event.preventDefault();

                window.location.href =
                    "/2fa/recovery/email";
            }
        );
    }


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
                "Verify";
        }
    }

});
