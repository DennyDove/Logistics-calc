let loading = document.getElementById("loading");
let result1 = document.getElementById("result1");
let result2 = document.getElementById("result2");

let form = document.getElementById("form");

form.addEventListener("submit", (e) => {
  loading.className ="show";
  result1.className ="hide";
  result2.className ="hide";
});
