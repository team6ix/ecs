function submitShelter()
{
	console.log("Sending request to add new shelter");
	document.getElementById("shelterForm").submit();
	window.location.href = "/index.html";
	alert("Sending request to add new shelter");
}

function submitDisaster()
{
	console.log("Sending request to add new disaster");
	document.getElementById("disasterForm").submit();
	window.location.href = "/admin.html";
	alert("Sending request to add new disaster");
}

