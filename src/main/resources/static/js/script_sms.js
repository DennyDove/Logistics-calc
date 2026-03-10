document.getElementById("btnSendCode").onclick = async () => {
    document.getElementById("error-message").style.display = "none"; // строчка нужна, чтобы при повторном вводе удалялось сообщение об ошибке

    let phone = document.getElementById("phone").value;

    let resp = await fetch("/api/auth/phone-send-code", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: new URLSearchParams({
            phone: phone
    })
    });

    if (resp.ok) {
        document.getElementById("phoneView").innerText = phone;
        document.getElementById("stage-phone").style.display = "none";
        document.getElementById("stage-code").style.display = "block";
    } else {
        alert("Ошибка при отправке кода");
    }
};

document.getElementById("btnLogin").onclick = async () => {
    let phone = document.getElementById("phone").value;
    let code = document.getElementById("code").value;

    let resp = await fetch("/api/auth/phone-login", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: new URLSearchParams({
            phone: phone,
            code: code
        })
    });

    if (resp.status === 200) {
        window.location.href = "/";
    } else if (resp.status === 401) {
        //alert("Неверный код");
        //toDo Сделать расшифровку ошибки: /login-main?error - done!
        window.location.href = "/login-main?error=INVALID_CODE";
    }
};
