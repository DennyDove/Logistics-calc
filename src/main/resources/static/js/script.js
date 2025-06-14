// Периодически приходится мучиться с тем, что Intellij или браузер
// не обновляет файл скрипта script.js
// При загрузке страницы на localhost потом нажать Ctrl + F5 --> и это должно решить проблему

let name = document.getElementById("name");
let login = document.getElementById("login");
let age = document.getElementById("age");
let email = document.getElementById("email");
let password = document.getElementById("password");
let confirmPassword = document.getElementById("confirmPassword");
let errorMessage = document.getElementById("errorMessage");

let form = document.getElementById("form");
let regButton = document.getElementById("regButton");

let correctPassword = false;


// Проверка совпадения паролей
function checkPassword() {
    const password = form.password.value;
    const confirmPassword = form.confirmPassword.value;
    if (password != confirmPassword) {
        errorMessage.className = "show-error";
        correctPassword = false;

    }
    else {
        errorMessage.className = "hide-error";
        correctPassword = true;
    }
}

// Регистрация нового пользователя
async function createUser() {

  let obj = {
    name : name.value,
    login : login.value,
    email : email.value,
    age : age.value,
    password : password.value
  };

  let request = await fetch("/adduser",
  // Если указать путь URI --> "https", то будет выскакивать ошибка Failed to load resource: net::ERR_SSL_PROTOCOL_ERROR
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(request.ok) {
    alert("User created!");
    window.location.replace("/");

  } else {
    alert("HTTP error: "+ request.status);
  }
}

confirmPassword.addEventListener("keyup", function() {
  checkPassword();
});

regButton.addEventListener("click", function() {
    if(correctPassword === true) createUser();
});
