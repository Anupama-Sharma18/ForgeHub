document.addEventListener(
    "DOMContentLoaded",
    function () {

        // =====================================================
        // GET ACCESS TOKEN
        // =====================================================

        const accessToken =
            sessionStorage.getItem("accessToken");


        // =====================================================
        // NO TOKEN
        // =====================================================

        if (!accessToken) {

            window.location.replace(
                "/login"
            );

            return;
        }


        // =====================================================
        // CHECK ADMIN ROLE
        // =====================================================

        try {

            const parts =
                accessToken.split(".");


            if (parts.length !== 3) {

                throw new Error(
                    "Invalid JWT format"
                );
            }


            // JWT payload
            const payload =
                parts[1];


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


            // =================================================
            // ADMIN ONLY
            // =================================================

            if (role !== "ADMIN") {

                console.warn(
                    "Unauthorized role attempted to access admin dashboard"
                );


                window.location.replace(
                    "/login"
                );

                return;
            }


            // =================================================
            // ADMIN ACCESS GRANTED
            // =================================================

            console.log(
                "Admin dashboard access granted"
            );


        } catch (error) {

            console.error(
                "Unable to validate access token:",
                error
            );


            sessionStorage.clear();


            window.location.replace(
                "/login"
            );
        }

    }
);