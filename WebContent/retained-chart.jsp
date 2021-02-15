<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>Monthly Breakdown</title>

<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous">

<script src="https://www.gstatic.com/charts/loader.js"></script>



<script>
google.charts.load('current', {packages: ['corechart']});
google.charts.setOnLoadCallback(drawChart);



function drawChart() {
  // Define the chart to be drawn.
    var data = google.visualization.arrayToDataTable([
      ['Month', 'Cancelled', 'Retained'],
      ['January', 989, 267]
      /*['February', 1170, 460],
      ['March', 660, 1120],
      ['April', 1030, 540],
      ['May', 660, 1120],
      ['June', 660, 1120],
      ['July', 660, 1120],
      ['August', 660, 1120],
      ['September', 660, 1120],
      ['October', 660, 1120],
      ['November', 660, 1120],
      ['December', 660, 1120],*/
      
    ]);
  
    var options = {
            chart: {
              title: 'Cancellation and Retained',
              subtitle: 'Cancellation and Retained',
            }
    };
  
  // Instantiate and draw the chart.
  var chart = new google.visualization.ColumnChart(document.getElementById('columnChart'));
  chart.draw(data, options);
}
			

</script>

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

h3 {
	text-align: center;
	padding-top: 30px;
	font-family: -apple-system, BlinkMacSystemFont;
}

hr {
	width: 70%
}
</style> 

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
			<!--  adding dropdwon menu for different charts -->
			<ul class="navbar-nav" id="dropdown">
				<li><a href="<%=request.getContextPath()%>/chart"
					class="nav-link">Last Month Breakdown</a>
					<div class="dropdown-content">
						<a href="<%=request.getContextPath()%>/retained-chart">Retained Vs. Cancelled Policies</a> <a href="<%=request.getContextPath()%>/chart">Policies Sold Last 3 Months</a> <a href="#">Retained</a>
					</div></li>
			</ul>
		</nav>
	</header>
	<!-- Identify where the chart should be drawn. -->
	<div class="container">
		<h3><a href="<%=request.getContextPath()%>/retained-chart" style="color: black; text-decoration: none;">Retained Vs. Cancelled Policies</a></h3>
		<hr>
		  
		<div id="columnChart"
			style="width: 80%; margin-left: auto; margin-right: auto; text-align: center; padding-top: 10px;"></div>

		<h3><a href="<%=request.getContextPath()%>/chart" style="color: black; text-decoration: none;">Policies Sold Last 3 Months</a></h3>
		<hr>
		<!--
		<div id="regions_div"
			style="width: 900px; height: 500px; margin-left: 10%; margin-right: 10%"></div> -->
	</div>




</body>
</html>



