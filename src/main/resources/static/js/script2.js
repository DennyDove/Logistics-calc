
let token = document.getElementById("token");
let password = document.getElementById("password");

let form = document.getElementById("form");
let saveButton = document.getElementById("saveButton");

let correctPassword = false;

// Проверка совпадения паролей
function checkPassword() {
    const password = form.password.value;
    const confirmPassword = form.confirmPassword.value;
    if (password != confirmPassword) {
        errorPassword.className = "show-error";
        correctPassword = false;

    }
    else {
        errorPassword.className = "hide-error";
        correctPassword = true;
    }
}



// Сохранение нового пароля
async function newPassword() {

  let obj = {
    token : token.value,
    password : password.value
  };

  let request = await fetch("/save-pass",
  // Если указать путь URI --> "https", то будет выскакивать ошибка Failed to load resource: net::ERR_SSL_PROTOCOL_ERROR
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(request.ok) {
    confirmText.className = "show";
    regform_holder.style.height = "390px";
    regButton.style.visibility = "hidden";
    window.location.href = "/";
  }

  else {
    alert("HTTP error: "+ request.status);
  }
}

confirmPassword.addEventListener("keyup", function() {
  checkPassword();
});

saveButton.addEventListener("click", function() {
    if(correctPassword === true) newPassword();
});
