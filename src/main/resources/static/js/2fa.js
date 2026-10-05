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
    //
    // loginEmail sirf UI ke liye hai.
    // Authentication ke liye iska use nahi karna hai.
    //
    // Actual authentication:
    // 2FA token     -> HttpOnly cookie
    // access token  -> HttpOnly cookie
    // refresh token -> HttpOnly cookie
    // =========================================================

    const loginEmail =
        sessionStorage.getItem("loginEmail");

    if (loginEmail && emailDisplay) {

        emailDisplay.textContent =
            loginEmail;
    }


    // =========================================================
    // OTP INPUT - ONLY 6 DIGITS
    // =========================================================

    if (codeInput) {

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
    }


    // =========================================================
    // FORM SUBMIT
    // =========================================================

    if (twoFactorForm) {

        twoFactorForm.addEventListener(
            "submit",
            async function (event) {

                event.preventDefault();


                // -------------------------------------------------
                // CLEAR OLD MESSAGE
                // -------------------------------------------------

                hideMessage(errorMessage);


                // -------------------------------------------------
                // GET OTP
                // -------------------------------------------------

                const code =
                    codeInput
                        ? codeInput.value.trim()
                        : "";


                // -------------------------------------------------
                // CLIENT SIDE VALIDATION
                // -------------------------------------------------

                if (!/^\d{6}$/.test(code)) {

                    showError(
                        "Please enter a valid 6-digit Google Authenticator code."
                    );

                    if (codeInput) {
                        codeInput.focus();
                    }

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
                    //
                    // Browser automatically sends:
                    //
                    // twoFactorSetupToken
                    // OR
                    // twoFactorLoginToken
                    // OR
                    // twoFactorRecoveryToken
                    //
                    // because these are HttpOnly cookies.
                    //
                    // Backend verification ke baad:
                    //
                    // accessToken  -> HttpOnly cookie
                    // refreshToken -> HttpOnly cookie
                    //
                    // =============================================

                    const response =
                        await fetch(
                            "/auth/2fa/verify",
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json",

                                    "Accept":
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

                        if (codeInput) {

                            codeInput.value = "";

                            codeInput.focus();
                        }

                        return;
                    }


                    // =============================================
                    // OTP VERIFICATION SUCCESSFUL
                    // =============================================
                    //
                    // IMPORTANT:
                    //
                    // data.accessToken ko sessionStorage mein
                    // store nahi karna hai.
                    //
                    // Backend already accessToken ko
                    // HttpOnly cookie mein set karta hai.
                    //
                    // JavaScript HttpOnly cookie ko read nahi kar
                    // sakta, aur read karna bhi nahi chahiye.
                    //
                    // =============================================


                    // =============================================
                    // CLEAN TEMPORARY UI DATA
                    // =============================================

                    sessionStorage.removeItem(
                        "qrCodeUrl"
                    );


                    // loginEmail bhi sirf temporary UI data hai
                    sessionStorage.removeItem(
                        "loginEmail"
                    );


                    // =============================================
                    // GO TO ADMIN DASHBOARD
                    // =============================================

                    window.location.href =
                        "/admin/dashboard";


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
    }


    // =========================================================
    // LOST YOUR OTP
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
