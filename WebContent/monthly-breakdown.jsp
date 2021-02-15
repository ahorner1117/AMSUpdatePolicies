<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Monthly Breakdown</title>

<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous">

<script src="https://www.gstatic.com/charts/loader.js"></script>
<script async defer
	src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBgc8c0_Kwoqmwj-R-KWHdD2WOhQon_-qY"
	type="text/javascript"></script>

<script>
	const mapsApiKey = "AIzaSyBgc8c0_Kwoqmwj-R-KWHdD2WOhQon_-qY";

	google.charts.load('current', {
		'packages' : [ 'geochart' ],
		// Note: you will need to get a mapsApiKey for your project.
		// See: https://developers.google.com/chart/interactive/docs/basic_load_libs#load-settings
		'mapsApiKey' : 'AIzaSyBgc8c0_Kwoqmwj-R-KWHdD2WOhQon_-qY'
	});

	google.charts.setOnLoadCallback(drawRegionsMap);

	// Need to call Zoho / AMS API for live data for policies 
	function drawRegionsMap() {
		var data = google.visualization.arrayToDataTable([
				[ 'State', 'Policies' ], [ 'US-AL', 68 ], [ 'US-AK', 0 ],
				[ 'US-AR', 22 ], [ 'US-AZ', 39 ], [ 'US-CA', 243 ],
				[ 'US-CO', 41 ], [ 'US-CT', 38 ],// DC has two policies - DC is not part of google API
				[ 'US-DE', 25 ], [ 'US-FL', 550 ], //8632 is true value -- testing
				[ 'US-GA', 58 ], [ 'US-HI', 14 ], [ 'US-ID', 27 ],
				[ 'US-IL', 41 ], [ 'US-IN', 46 ], [ 'US-IA', 11 ],
				[ 'US-KS', 19 ],
				// ['US-KY', 0], Null - no policies
				[ 'US-LA', 23 ], [ 'US-ME', 17 ], [ 'US-MD', 58 ],
				[ 'US-MA', 85 ], [ 'US-MI', 110 ], [ 'US-MN', 18 ],
				[ 'US-MS', 31 ], [ 'US-MO', 22 ], [ 'US-MT', 2 ],
				[ 'US-NV', 16 ], [ 'US-NH', 11 ], [ 'US-NE', 16 ],
				[ 'US-NJ', 284 ], [ 'US-NM', 30 ], [ 'US-NY', 184 ],
				[ 'US-NC', 85 ], [ 'US-ND', 2 ], [ 'US-OH', 80 ],
				[ 'US-OK', 31 ], [ 'US-OR', 24 ], [ 'US-PA', 121 ],
				[ 'US-RI', 24 ], [ 'US-SC', 66 ], [ 'US-SD', 0 ],
				[ 'US-TN', 69 ], [ 'US-TX', 529 ], [ 'US-UT', 2 ],
				[ 'US-VT', 2 ], [ 'US-VA', 132 ], [ 'US-WA', 47 ],
				[ 'US-WV', 41 ], [ 'US-WI', 32 ], [ 'US-WY', 1 ], ]);

		var options = {
			region : 'US',
			displayMode : 'regions',
			resolution : 'provinces',
		};

		var chart = new google.visualization.GeoChart(document
				.getElementById('regions_div'));

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
	font-family: -apple-system, BlinkMacSystemFont
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
			style="width: 70%; margin-left: auto; margin-right: auto; text-align: center;"></div>

		<h3>Policies Sold Last 3 Months</h3>
		<hr>
		<div id="regions_div"
			style="width: 900px; height: 500px; margin-left: 10%; margin-right: 10%"></div>
	</div>




</body>
</html>



