
async function taskOrder(company) {

  let data = {
      key: company
  };

  let response = await fetch("/order",
    {
    	method: 'POST',
    	headers: {
    	    //'Accept': 'application/json',
    	    'Content-Type': 'application/x-www-form-urlencoded'
    	},
    	body: new URLSearchParams(data).toString()
    });

  if(response.ok) {
    let orderObject = await response.json();
    calcButton.style.visibility = "hidden";
    results.className = "hide";
    orderOk.className = "show";
    //                                             + data.key + ".gif\" - получает имя файла логотипа по названию компании (например: "dellline".jpg)
    orderOk_text.innerHTML = "<img src=\"images/" + data.key + ".jpg\" width=\"87\" height=\"23\"> <br> <span id=\"price_span\">" + orderObject.price + "</span> руб." + "  мин. срок доставки: " + orderObject.days +
                             " дн. <br> Ваш заказ №" + orderObject.id + " оформлен в работу";

  } else {
    alert("HTTP error: "+ response.status);
  }
}

orderButton1.addEventListener("click", function() {
    taskOrder("vozovoz");
});

orderButton2.addEventListener("click", function() {
    taskOrder("delline");
});

orderButton3.addEventListener("click", function() {
    taskOrder("nordw");
});
