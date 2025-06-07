// Периодически приходится мучиться с тем, что Intellij или браузер
// не обновляет файл скрипта script.js
// При загрузке страницы на localhost потом нажать Ctrl + F5 --> и это должно решить проблему

let name = document.getElementById("name");
let login = document.getElementById("login");
let age = document.getElementById("age");
let password = document.getElementById("password");
let confirmPassword = document.getElementById("confirmPassword");
//let errorMessage = document.getElementById("errorMessage");
//let form = document.getElementById("confirmPassword");

let regButton = document.getElementById("regButton");

// Проверка совпадения паролей
function checkPassword() {
    const password = password.value;
    const confirmPassword = confirmPassword.value;
    if (password != confirmPassword) {
        alert("Error! Password did not match.");
        return false;
    }
    else {
        alert("Password Match. Congratulations!");
        return true;
    }
}

/*
confirmPassword.addEventListener('keyup', () => {
})
*/


// Регистрация нового пользователя
async function createUser() {

  let obj = {
    name : name.value,
    login : login.value,
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
    window.location.replace("/products");

  } else {
    alert("HTTP error: "+ request.status);
  }
}

regButton.addEventListener("click", function() {
    //checkPassword();
    createUser();
});

async function deleteItem(prodId) {

  // простой вариант DELETE https запроса без тела. Здесь мы вручную вписали параметры в функцию fetch: fetch("/deleteitem?id="+ prodId.....
  let request = await fetch("/deleteitem?id=" + prodId, {
      method: 'DELETE'
  });

  if(request.ok) {
    //alert("Монета удалена из корзины");
    window.location.replace("/cart");
  } else {
    alert("HTTP error: "+ request.status);
  }

  /* вариант DELETE https запроса c телом запроса и объектом класса URLSearchParams.
  // Однако в DELETE https запросах обычно нет тела, и сервер может из-за этого выдавать ошибку
  // HttpRequestMethodNotSupportedException: Request method 'DELETE' is not supported
  var params = new URLSearchParams('id=' + prodId);
  let request = await fetch("/deleteitem", {
      method: 'DELETE',
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"},
      body: params
  });
  */
}
