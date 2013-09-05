<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>AlgoTrader Reference Data Manager</title>
		<style type="text/css" media="screen">
			#status {
				background-color: #eee;
				border: .2em solid #fff;
				margin: 2em 2em 1em;
				padding: 1em;
				width: 12em;
				float: left;
				-moz-box-shadow: 0px 0px 1.25em #ccc;
				-webkit-box-shadow: 0px 0px 1.25em #ccc;
				box-shadow: 0px 0px 1.25em #ccc;
				-moz-border-radius: 0.6em;
				-webkit-border-radius: 0.6em;
				border-radius: 0.6em;
			}

			.ie6 #status {
				display: inline; /* float double margin fix http://www.positioniseverything.net/explorer/doubled-margin.html */
			}

			#status ul {
				font-size: 0.9em;
				list-style-type: none;
				margin-bottom: 0.6em;
				padding: 0;
			}

			#status li {
				line-height: 1.3;
			}

			#status h1 {
				text-transform: uppercase;
				font-size: 1.1em;
				margin: 0 0 0.3em;
			}

			#page-body {
				margin: 2em 1em 1.25em 2em;
			}

			h2 {
				margin-top: 1em;
				margin-bottom: 0.3em;
				font-size: 1em;
			}

			p {
				line-height: 1.5;
				margin: 0.25em 0;
			}

			#controller-list ul {
				list-style-position: inside;
			}

			#controller-list li {
				line-height: 1.3;
				list-style-position: inside;
				margin: 0.25em 0;
			}

			@media screen and (max-width: 480px) {
				#status {
					display: none;
				}

				#page-body {
					margin: 0 1em 1em;
				}

				#page-body h1 {
					margin-top: 0;
				}
			}
		</style>
	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>				
			</ul>
		</div>
		<div id="page-body" role="main">

			<%
				def controllers = [
					[0,'ch.algotrader.entity.Account'],
					[0,'ch.algotrader.entity.strategy.Allocation'],
					[0,'ch.algotrader.entity.security.BrokerParameters'],
					[0,'ch.algotrader.entity.security.Component'],
					[0,'ch.algotrader.entity.strategy.DefaultOrderPreference'],
					[0,'ch.algotrader.entity.strategy.OrderPreference'],
					[0,'ch.algotrader.entity.property.Property'],
					[0,'ch.algotrader.entity.security.SecurityFamily'],
						[1,'ch.algotrader.entity.security.BondFamily'],
						[1,'ch.algotrader.entity.security.ForexFutureFamily'],
						[1,'ch.algotrader.entity.security.FutureFamily'],
						[1,'ch.algotrader.entity.security.GenericFutureFamily'],
						[1,'ch.algotrader.entity.security.StockOptionFamily'],
					[0,'ch.algotrader.entity.security.Security'],
						[1,'ch.algotrader.entity.security.Bond'],
						[1,'ch.algotrader.entity.security.Combination'],
						[1,'ch.algotrader.entity.security.ForexFuture'],
						[1,'ch.algotrader.entity.security.Forex'],
						[1,'ch.algotrader.entity.security.Future'],
						[1,'ch.algotrader.entity.security.GenericFuture'],
						[1,'ch.algotrader.entity.security.ImpliedVolatility'],
						[1,'ch.algotrader.entity.security.IntrestRate'],
						[1,'ch.algotrader.entity.security.NaturalIndex'],
						[1,'ch.algotrader.entity.security.Stock'],
						[1,'ch.algotrader.entity.security.StockOption'],
					[0,'ch.algotrader.entity.strategy.Strategy']
				]
			%>

			<div id="controller-list" role="navigation">
				<ul>				
					<g:each var="c" in="${controllers }">
						<% cc = grailsApplication.getControllerClass(c[1] + "Controller") %>
						<li class="controller" <% if (c[0] == 1) { %> style="margin-left: 30px" <% }%> ><g:link controller="${cc.logicalPropertyName}">${cc.name}</g:link></li>
					</g:each>					
				</ul>
			</div>
		</div>
	</body>
</html>
