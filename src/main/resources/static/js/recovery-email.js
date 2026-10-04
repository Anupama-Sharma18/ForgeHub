document.addEventListener("DOMContentLoaded", function () {

    // =========================================================
    // GET ELEMENTS
    // =========================================================

    const recoveryEmailForm =
        document.getElementById("recoveryEmailForm");

    const emailInput =
        document.getElementById("email");

    const sendOtpButton =
        document.getElementById("sendOtpButton");

    const errorMessage =
        document.getElementById("errorMessage");

    const successMessage =
        document.getElementById("successMessage");


    // =========================================================
    // FORM SUBMIT
    // =========================================================

    recoveryEmailForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            // -------------------------------------------------
            // CLEAR PREVIOUS MESSAGES
            // -------------------------------------------------

            hideMessage(errorMessage);

            hideMessage(successMessage);


            // -------------------------------------------------
            // GET EMAIL
            // -------------------------------------------------

            const email =
                emailInput.value.trim();


            // -------------------------------------------------
            // VALIDATE EMAIL
            // -------------------------------------------------

            if (email === "") {

                showError(
                    "Please enter your registered email."
                );

                emailInput.focus();

                return;
            }


            if (!isValidEmail(email)) {

                showError(
                    "Please enter a valid email address."
                );

                emailInput.focus();

                return;
            }


            // -------------------------------------------------
            // BUTTON LOADING
            // -------------------------------------------------

            setLoading(true);


            try {

                // =============================================
                // SEND RECOVERY EMAIL OTP
                // =============================================
                //
                // Backend endpoint:
                //
                // POST /auth/2fa/recovery/send?email=...
                //
                // Browser automatically sends the
                // twoFactorLoginToken HttpOnly cookie.
                // =============================================

                const response =
                    await fetch(
                        "/auth/2fa/recovery/send?email=" +
                        encodeURIComponent(email),
                        {
                            method: "POST",

                            credentials:
                                "same-origin"
                        }
                    );


                // =============================================
                // READ RESPONSE
                // =============================================

                let message = "";

                try {

                    message =
                        await response.text();

                } catch (error) {

                    console.error(
                        "Unable to read response:",
                        error
                    );
                }


                // =============================================
                // FAILED
                // =============================================

                if (!response.ok) {

                    showError(
                        message ||
                        "Unable to send email OTP."
                    );

                    return;
                }


                // =============================================
                // SAVE EMAIL
                // =============================================
                //
                // recovery-otp.js ko email chahiye hogi.
                // =============================================

                sessionStorage.setItem(
                    "loginEmail",
                    email
                );


                // =============================================
                // SHOW SUCCESS
                // =============================================

                showSuccess(
                    message ||
                    "OTP has been sent to your registered email."
                );


                // =============================================
                // REDIRECT TO EMAIL OTP PAGE
                // =============================================

                setTimeout(function () {

                    window.location.href =
                        "/2fa/recovery/otp";

                }, 700);


            } catch (error) {

                console.error(
                    "Recovery email request failed:",
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

        element.textContent = "";

        element.classList.add(
            "d-none"
        );
    }


    // =========================================================
    // BUTTON LOADING
    // =========================================================

    function setLoading(isLoading) {

        sendOtpButton.disabled =
            isLoading;


        if (isLoading) {

            sendOtpButton.innerHTML = `
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true">
                </span>
                Sending OTP...
            `;

        } else {

            sendOtpButton.innerHTML =
                "Send Email OTP";
        }
    }

});