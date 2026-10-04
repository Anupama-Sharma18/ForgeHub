document.addEventListener(
    "DOMContentLoaded",
    function () {

        // =====================================================
        // GET LOGOUT BUTTON
        // =====================================================

        const logoutButton =
            document.getElementById("logoutButton");


        if (!logoutButton) {

            return;
        }


        // =====================================================
        // LOGOUT
        // =====================================================

        logoutButton.addEventListener(
            "click",
            async function () {

                // ---------------------------------------------
                // Disable button
                // ---------------------------------------------

                logoutButton.disabled =
                    true;


                logoutButton.innerHTML = `
                    <span
                        class="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true">
                    </span>
                    Logging out...
                `;


                try {

                    // =========================================
                    // BACKEND LOGOUT API
                    // =========================================

                    const response =
                        await fetch(
                            "/auth/logout",
                            {
                                method: "POST",

                                credentials:
                                    "same-origin"
                            }
                        );


                    if (!response.ok) {

                        console.warn(
                            "Logout API returned:",
                            response.status
                        );
                    }


                } catch (error) {

                    console.error(
                        "Logout request failed:",
                        error
                    );


                } finally {

                    // =========================================
                    // REMOVE ACCESS TOKEN
                    // =========================================

                    sessionStorage.removeItem(
                        "accessToken"
                    );


                    // =========================================
                    // REMOVE TEMPORARY AUTH DATA
                    // =========================================

                    sessionStorage.removeItem(
                        "loginEmail"
                    );

                    sessionStorage.removeItem(
                        "qrCodeUrl"
                    );


                    // =========================================
                    // GO TO LOGIN
                    // =========================================

                    window.location.replace(
                        "/login"
                    );
                }

            }
        );

    }
);