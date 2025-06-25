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
let correctAge = false;
let correctEmail = false;

let emailPattern = new RegExp("[a-zA-Z0-9.-~_]+@[a-zA-Z0-9.]+[\.]");  //+([\w-]+\.)+[a-zA-Z0-9]");   //+[\w]");
                                  // почему-то здесь не работает /w

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

function checkAge() {
    const age = form.age.value;
    if (age < 16 || age > 100) {
        errorAge.className = "show-error";
        correctAge = false;
    }
    else {
        errorAge.className = "hide-error";
        correctAge = true;
    }
}

function checkEmail() {
    const email = form.email.value;
    if (emailPattern.test(email) === false) {
        errorEmail.className = "show-error";
        correctEmail = false;
    }
    else {
        errorEmail.className = "hide-error";
        correctEmail = true;
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

age.addEventListener("keyup", function() {
  checkAge();
});

email.addEventListener("keyup", function() {
  checkEmail();
});

regButton.addEventListener("click", function() {
    if(correctPassword === true && correctAge === true && correctEmail) createUser();
});
