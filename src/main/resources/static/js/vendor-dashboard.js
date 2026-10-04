document.addEventListener(
    "DOMContentLoaded",
    function () {

        const accessToken =
            sessionStorage.getItem("accessToken");


        if (!accessToken) {

            window.location.href =
                "/login";

            return;
        }


        try {

            const payload =
                accessToken.split(".")[1];

            const decoded =
                JSON.parse(
                    atob(
                        payload
                            .replace(/-/g, "+")
                            .replace(/_/g, "/")
                    )
                );


            if (decoded.role !== "VENDOR") {

                window.location.href =
                    "/login";

                return;
            }


            console.log(
                "Vendor dashboard access granted"
            );

        } catch (error) {

            console.error(error);

            sessionStorage.clear();

            window.location.href =
                "/login";
        }

    }
);