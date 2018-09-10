function btnClick(){
    var result = window.android.getString();
    document.getElementById("account").value = result;
}

function sum(a,b){
    return a + b;
}

window.onload = function (){
	document.getElementById("login").onclick = btnClick;
}
