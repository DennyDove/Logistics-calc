
let startPoint = document.getElementById("startPoint");
let destination = document.getElementById("destination");
let cargoName = document.getElementById("cargoName");
let weight = document.getElementById("weight");


async function taskOrder() {

  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    cargoName : cargoName.value,
    weight : weight.value
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

orderButton.addEventListener("click", function() {
    taskOrder();
});
