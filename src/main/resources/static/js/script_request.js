
let calcDiv = document.getElementById("calcDiv");

let startPoint = document.getElementById("startPoint");
let destination = document.getElementById("destination");
let length = document.getElementById("length");
let width = document.getElementById("width");
let height = document.getElementById("height");
let weight = document.getElementById("weight");

let form = document.getElementById("form");
let vozovoz = document.getElementById("vozovoz");
let delline = document.getElementById("delline");
let nordw = document.getElementById("nordw");

let calcDivHeight = 353;

/*
Как высянилось, необязательно создавать переменные для этих элементов
let calcButton = document.getElementById("calcButton");
let orderButton1 = document.getElementById("orderButton1");
let orderButton2 = document.getElementById("orderButton2");
let orderButton3 = document.getElementById("orderButton3");*/

// Отправка post-запроса
async function vozCalc() {

  //result1.className = "hide";
  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    length : length.value,
    width : width.value,
    height : height.value,
    weight : weight.value
  };

  /*
  let request = fetch("/vozcalc",
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    }).then(request => {
            if(!request.ok) {
              alert("HTTP error: "+ request.status);
              throw new Error(`Something went wrong:`) //${response.status}`)
            }
            loading.className = "hide";
            //calcDiv.style.height = "423px";
            result1.className = "show";
            let taskObj = request.json();
            result1_.innerHTML = " " + taskObj.price + " руб.";
          })
          .catch(error => {
            console.log(error)
          });
  */

  let response = await fetch("/vozcalc",
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(response.ok) {
    //alert("Request successful!");
    //window.location.reload();
    loading.className ="hide";
    calcDivHeight +=37;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    vozovoz.className ="show";
    let taskObj = await response.json();
    result1.innerHTML = " <span id=\"price_span\">" + taskObj.price + "</span> руб." + "  мин. срок доставки: " + taskObj.days + " дн.";
    orderButton1.style.visibility = "visible";
  } else {
    loading.className = "hide";
    let errMsg = await response.text();
    //alert("HTTP error: "+ errMsg);
    calcDivHeight +=77;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    vozovoz.className = "show";
    result1.className = "errorText";
    result1.innerHTML = " " + errMsg;
    orderButton1.style.visibility = "hidden";
  }
}

async function dellineCalc() {

  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    length : length.value,
    width : width.value,
    height : height.value,
    weight : weight.value
  };

  let response = await fetch("/delline",
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(response.ok) {
    //alert("Request successful!");
    //window.location.reload();
    loading.className = "hide";
    calcDivHeight +=37;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    delline.className = "show";
    let taskObj = await response.json();
    result2.innerHTML = " <span id=\"price_span\">" + taskObj.price + "</span> руб." + "  мин. срок доставки: " + taskObj.days + " дн.";
    orderButton2.style.visibility = "visible";
  } else if(response.status === 400) {
    loading.className = "hide";
    let errMsg = await response.text();
    calcDivHeight +=77;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    delline.className = "show";
    result2.className = "errorText";
    result2.innerHTML = " " + errMsg;
    orderButton2.style.visibility = "hidden";
  }
}

async function nordwCalc() {

  let obj = {
    startPoint : startPoint.value,
    destination : destination.value,
    length : length.value,
    width : width.value,
    height : height.value,
    weight : weight.value
  };

  let response = await fetch("/nordwcalc",
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(response.ok) {
    loading.className = "hide";
    calcDivHeight +=33;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    nordw.className = "show";
    let taskObj = await response.json();
    result3.innerHTML = " <span id=\"price_span\">" + taskObj.price + "</span> руб." + "  мин. срок доставки: " + taskObj.days + " дн.";
    orderButton3.style.visibility = "visible";
  } else {
    let errMsg = await response.text();
    calcDivHeight +=77;
    calcDiv.style.height = (calcDivHeight).toString() + "px";
    nordw.className = "show";
    result3.className = "errorText";
    result3.innerHTML = " " + errMsg;
    loading.className = "hide";
    orderButton3.style.visibility = "hidden";
  }
}

calcButton.addEventListener("click", async function() {
    calcDivHeight = 353;
    calcDiv.style.height = "350px";
    loading.className = "show";
    vozovoz.className ="hide";
    delline.className ="hide";
    nordw.className ="hide";
    result1.innerHTML = ""; result1.className = "normalText";
    result2.innerHTML = ""; result2.className = "normalText";
    result3.innerHTML = ""; result3.className = "normalText";

    await vozCalc();
    loading.className = "show";
    await dellineCalc();
    loading.className = "show";
    await nordwCalc();
});
