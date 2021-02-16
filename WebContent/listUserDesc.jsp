<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>

<head>
<title>User Management Application</title>
<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous">
	
	<script src='https://kit.fontawesome.com/a076d05399.js' crossorigin='anonymous'></script>

<style>
.dropdown-content a:hover {
	background-color: #ddd;
}

#dropdown:hover .dropdown-content {
	display: block;
}

#dropdown:hover .dropbtn {
	background-color: #3e8e41;
}

.dropdown-content a {
	color: black;
	padding: 12px 16px;
	text-decoration: none;
	display: block;
}

.dropdown-content {
	display: none;
	position: absolute;
	background-color: #f1f1f1;
	min-width: 160px;
	box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.2);
	z-index: 1;
}

#dropdown {
	position: relative;
	display: inline-block;
}
</style>

<script>
var count = 0;
function sortTable(){
	count++;
	console.log("count = " + count);
	
	var num = count % 2;
	console.log("module = " + num);
	//var ascending = false;
	document.getElementById('up_down').classList.add('fa-angle-down');
	document.getElementById('up_down').classList.add('fa-angle-up');
	if (num == 1 ){
		document.getElementById('up_down').classList.add("fa-angle-down");
		document.getElementById('up_down').classList.remove("fa-angle-up");
		console.log("Angle up");
		
	}
	if( num == 0){
		document.getElementById('up_down').classList.add('fa-angle-up');
		document.getElementById('up_down').classList.remove("fa-angle-down");
		console.log("Angle down");
		
	}
	
	
}

</script>
</head>

<body>
	<header>
		<nav class="navbar navbar-expand-md navbar-dark"
			style="background-color: tomato">
			<div>
				<a href="<%=request.getContextPath()%>/list" class="navbar-brand">
					Insurance Express User Management </a>
			</div>

			<ul class="navbar-nav">
				<li><a href="<%=request.getContextPath()%>/list"
					class="nav-link">Users</a></li>
			</ul>
			<ul class="navbar-nav" id="dropdown">
				<li><a href="<%=request.getContextPath()%>/chart"
					class="nav-link">Last Month Breakdown</a>
					<div class="dropdown-content">
						<a href="<%=request.getContextPath()%>/retained-chart">Retained Vs. Cancelled Policies</a> <a href="<%=request.getContextPath()%>/chart">Policies Sold Last 3 Months</a> <a href="#">Retained</a>
					</div></li>
			</ul>
		</nav>
	</header>
	<br>

	<div class="row">
		<!-- <div class="alert alert-success" *ngIf='message'>{{message}}</div> -->

		<div class="container">
			<h3 class="text-center">List of Users</h3>
			<hr>
			<div class="container text-left">

				<a href="<%=request.getContextPath()%>/new" class="btn btn-success">Add
					New User</a>
			</div>
			<br>
			<table class="table table-bordered">
				<thead>
					<tr>
						<th>ID</th>					<!-- ------- Need to use some href here to change condition for order asc or desc -->
						<th>Name&nbsp;<a href="<%=request.getContextPath()%>/list"><i class='fas fa-angle-up' id="up_down" onclick="sortTable()"></i></a></th> <!-- fas fa-angle-up -->
						<th>Email</th>
						<th>Country</th>	
						<th>Actions</th>
					</tr>
				</thead>
				<tbody>
					<!--   for (Todo todo: todos) {  -->
					<c:forEach var="user" items="${listUser}">

						<tr>
							<td><c:out value="${user.id}" /></td>
							<td><c:out value="${user.name}" /></td>
							<td><c:out value="${user.email}" /></td>
							<td><c:out value="${user.country}" /></td>
							<td><a href="edit?id=<c:out value='${user.id}' />">Edit</a>
								&nbsp;&nbsp;&nbsp;&nbsp; <a
								href="delete?id=<c:out value='${user.id}' />">Delete</a></td>
						</tr>
					</c:forEach>
					<!-- } -->
				</tbody>

			</table>
		</div>
	</div>
</body>

</html>