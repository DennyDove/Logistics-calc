
let startPoint = document.getElementById("startPoint");
let destination = document.getElementById("destination");
let cargoName = document.getElementById("cargoName");
let weigth = document.getElementById("weigth");

let length = document.getElementById("length_input1");
let width = document.getElementById("width_input1");
let heigth = document.getElementById("heigth_input1");


async function taskOrder() {

  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    cargoName : cargoName.value,
    weigth : weight.value
  };

  let response = await fetch("/order",
  // Если указать путь URI --> "https", то будет выскакивать ошибка Failed to load resource: net::ERR_SSL_PROTOCOL_ERROR
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(response.ok) {
    let responseObject = await response.json();
    let output = document.getElementById("message");
    //output.textContent = "Заказ №"+ responseObject.id + " оформлен";
    //message.className = "show-message"; // появляется сообщение

    window.location.replace("/order-ok?id=" + responseObject.id);

  } else {
    alert("HTTP error: "+ responset.status);
  }
}


async function calc() {
  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    cargoName : cargoName.value,
    weigth : weigth.value,
    length : length.value,
    width : width.value,
    heigth : heigth.value
  };

  let response = await fetch("/calc",
  // Если указать путь URI --> "https", то будет выскакивать ошибка Failed to load resource: net::ERR_SSL_PROTOCOL_ERROR
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

    /*
    if(response.ok) {
      window.location.reload();

    } else {
      alert("HTTP error: "+ responset.status);
    }
    */
  }


orderButton.addEventListener("click", function() {
    loading.className = "show";
    calc();
    //taskOrder();
});
