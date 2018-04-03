//==jQuery after()
function insertAfter(el, referenceNode) {
	referenceNode.parentNode.insertBefore(el, referenceNode.nextSibling);
}
//==jQuery remove()
function remove(id) {
    var elem = document.getElementById(id);
    if(elem != null){
    	return elem.parentNode.removeChild(elem);
    }
}

//Couleurs pour graphiques stats
var seed = 11;
function random() {
    var x = Math.sin(seed++) * 1000;
    return x - Math.floor(x);
}
var generateColors = [];
var generateBorderColors = [];
var generateStackColors = [];
for(var k = 0; k < 100; k++) {
	var color = Math.floor(random()*256) + ',' + Math.floor(random()*256) + ',' + Math.floor(random()*256);
	generateColors.push('rgba(' + color + ', 0.6)');
	generateStackColors.push('rgba(' + color + ', 0.6)');
	generateBorderColors.push('rgba(' + color + ', 1)'); 
}

//Format Dat String
function formatDateString(date){
	var splitDate = date.split("-");
	return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0];
}

//Convertion rgb-->hex
function rgb2hex(rgb){
	 rgb = rgb.match(/^rgba?[\s+]?\([\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?/i);
	 return (rgb && rgb.length === 4) ? "#" +
	  ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +
	  ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +
	  ("0" + parseInt(rgb[3],10).toString(16)).slice(-2) : '';
}

//autocomplete eppn
function searchEppnAutocomplete(id, url, field) {
	var searchBox = document.getElementById(id);
	if(searchBox != null){
	 	var awesomplete = new Awesomplete(searchBox, {
	 		  minChars: 3,
	 		  maxItems: 10
	 		});
	 	searchBox.addEventListener("keyup", function(){
			if(this.value.length>2) {
				var request = new XMLHttpRequest();
				request.open('GET', url + "?searchString=" + this.value, true);
				request.onload = function() {
				  if (request.status >= 200 && request.status < 400) {
				    var data = JSON.parse(this.response);
		 		    var list = [];
		 		    data.forEach(function(value, key) {
		 		      var labelValue = value.cn + " // " + value.eduPersonPrincipalName;
		 		      if(field == "eppn"){
		 		    	 labelValue = value.eppn;
		 		      }
			 		  list.push(labelValue);
		 		    });
		 		    awesomplete.list = list;		    
				    
				  } else {
				    // We reached our target server, but it returned an error
					  console.log("erreur du serveur!");
				  }
				};
				request.send();
			}
		});
	}
}
//submit search eppn
function searchEppnAction(idInputFile) {
	var el = document.getElementById(idInputFile);
	var request = new XMLHttpRequest();
	request.open('GET', submitUrl + "?searchText=" + el.value, true);
	request.onload = function() {
		  if (request.status >= 200 && request.status < 400) {
		    var data = JSON.parse(this.response);
		    document.getElementById("recto1").innerHTML = data.recto1;
		    document.getElementById("recto2").innerHTML = data.recto2;
		    document.getElementById("recto3").innerHTML = data.recto3;
		    document.getElementById("recto4").innerHTML = data.recto4;
		    document.getElementById("recto5").innerHTML = data.recto5;
		    document.getElementById("recto6").innerHTML = data.recto6;
		    document.getElementById("recto7").innerHTML = data.recto7;
		    document.getElementById("photo").setAttribute("src", "/manager/photo/" + data.id);
		    var existingP = document.getElementById("noResultMsg");
		    remove("noResultMsg");
		  } else {
				var ref = document.getElementById("searchEppnForm");
				var existingP = document.getElementById("noResultMsg");
				remove("noResultMsg");
				var newEl = document.createElement('p');
				newEl.setAttribute("class", "help-block");
				newEl.setAttribute("id", "noResultMsg");
				newEl.innerHTML = 'Aucun résultat trouvé';
				insertAfter(newEl, ref);
		  }
	};
	request.send();
}

//affiche spinner load charts
function displaySpinner(c,j) {
    var context=c.getContext("2d");
    var start = new Date();
    var lines = 16,  
        cW = context.canvas.width,
        cH = context.canvas.height;
    var draw = function() {
        var rotation = parseInt(((new Date() - start) / 1000) * lines) / lines;
        context.save();
        context.clearRect(0, 0, cW, cH);
        context.translate(cW / 2, cH / 2);
        context.rotate(Math.PI * 2 * rotation);
        for (var i = 0; i < lines; i++) {
            context.beginPath();
            context.rotate(Math.PI * 2 / lines);
            context.moveTo(cW / 10, 0);
            context.lineTo(cW / 4, 0);
            context.lineWidth = cW / 30;
            context.strokeStyle = "rgba(0, 0, 0," + i / lines + ")";
            context.stroke();
        }
        context.restore();
    };
    window['p'+j] = setInterval(draw, 1000 / 30);
};

//affiche stats
function getStats(id, chartType, selectedType, spinner, option, transTooltip, formatDate, label1, data2, label2, fill, arrayDates, byMonth){
	var prefId = document.getElementById(id);
	if((prefsStatsRm!=null && prefsStatsRm !="" && !(prefsStatsRm.indexOf(prefId)>-1)) || (prefsStatsRm==null || prefsStatsRm =="")){
		displaySpinner(document.querySelector("canvas#" + id), spinner);
		var request = new XMLHttpRequest();
		request.open('GET',  statsRootUrl + "/json?type=" + id + "&typeInd=" + selectedType, true);
		request.onload = function(){
			  if(request.status >= 200 && request.status < 400) {
			    var data = JSON.parse(this.response);
				clearInterval(window['p' + spinner]);
				if(chartType == "multiBar"){
					// multiChartStackBar(allData, id, start, transTooltip,formatDate){
					multiChartStackBar(data[id], id, 3, transTooltip, formatDate);
				}else if(chartType == "chartBar"){
					//chartBar(data1, label1, id, transTooltip, formatDate, data2, label2){
					chartBar(data[id], label1, id, transTooltip, formatDate, data[data2], label2);
				}else if(chartType == "pie"){
					// chartPieorDoughnut(data, id, type, option)
					chartPieorDoughnut(data[id], id, chartType, option);
				}else if(chartType == "doughnut"){
					chartPieorDoughnut(data[id], id, chartType, option);
				}else if(chartType == "lineChart"){
					lineChart(data[id], id, fill, arrayDates, byMonth, formatDate);
				}
			  }
		}
		request.send();
	}
 }

//Stats bar chart
function multiChartStackBar(allData, id, start, transTooltip,formatDate){
	if(typeof document.getElementById(id) != "undefined"){
    	var barLabels = [];
    	var length = Object.keys(allData).length;
    	var dataSets = [];
    	var j=0;
    	for (var idx in allData){ 
    		//Hack : on enlève le '_'
    		lab = idx.substr(1, idx.length);
    		if(formatDate){
    			lab = formatDateString(idx.substr(1, idx.length));
    		}
    		barLabels.push(lab);
    		var k=start;
    		var obj = allData[idx];
    		for (var key in obj){
	        	var values = [];
	        	for (var i = 0; i < length; i++)
	        	{
	        		values.push(0);
	        	}
	    		values[j] = obj[key];
	        	dataSets.push({
	        		label: key,
	        		data: values,
	        		backgroundColor: generateStackColors[k]
	        	});
	        	k++;
	    	};
	    	j++;
    	 };
        var  barChartData = {
            	labels : barLabels,
            	datasets : dataSets
        }
     	var ctx =  document.getElementById(id).getContext("2d");
    	myBar = new Chart(ctx, {
    		type: 'bar',
    		data: barChartData,
    		options: {responsive : true,
    			legend: {
    				display: false
    			},
    	        scales: {
    	            yAxes: [{
    	                ticks: {
    	                    beginAtZero:true
    	                },
    	                stacked: true
    	            }],
    	            xAxes: [{
    	                stacked: true
    	            }]
    	        },
                tooltips: {
                	mode: 'label',
                	bodyFontSize :  15,
                	titleFontSize: 16,
                	footerFontSize: 15,
                	callbacks: {
                        afterTitle: function() {
                            window.total = 0;
                        },
                        label: function (t, e) {
                        	if( t.yLabel!=0){
	                            var a = e.datasets[t.datasetIndex].label || '';
	                            var valor = parseInt(e.datasets[t.datasetIndex].data[t.index]);
	                            window.total += valor;
	                            if(transTooltip != null){
	                            	b = a.toString().replace(/_/g,"");
	                            	msg = transTooltip + b.charAt(0).toUpperCase() + b.slice(1).toLowerCase();
	                            	a = messages[msg];
	                            }
	                            return a + ': ' + t.yLabel 
                        	}
                        },
                        footer: function() {
                            return "TOTAL: " + window.total.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
                        }
                	}
                }
    	   }
    	});
	}	
}

function chartPieorDoughnut(data, id, type, option){
    if(typeof document.getElementById(id) != "undefined"){
    	var legend = true;
		if(option=="legend"){
		 legend = false;
		}
    	var doughnutLabels = [];
    	var values = [];
    	var dataSets = [];
    	var doughnutDataArray =[];
    	for (var idx in data){
    		doughnutLabels.push(idx);
    		values.push(data[idx]);
        };
    	dataSets.push({
    		data: values,
    		backgroundColor:  generateStackColors,
    		hoverBackgroundColor: generateColors
    	});
    	var doughnutDataArray={
			   labels: doughnutLabels,
			   datasets: dataSets
        	};	 	        	
     	var ctx3 = document.getElementById(id).getContext("2d");
     	var myDoughnutChart2 = new Chart(ctx3, {
    		type: type,
    		test: "dd",
    		data: doughnutDataArray,
    		options: {responsive : true, animateRotate : false,
    			legend: {
    				display: legend
    			},
    			tooltips: {
    				enabled: false,
    				custom: function chartcustomtooltip(tooltip) {
    					// Tooltip Element
    					var tooltipEl = document.getElementById(id).nextElementSibling;
    					// Hide if no tooltip
    					if (tooltip.opacity === 0) {
    						tooltipEl.style.opacity = 0;
    						return;
    					}
    					// Set caret Position
    					//tooltipEl.classList.remove('above', 'below', 'no-transform');
    					if (tooltip.yAlign) {
    						tooltipEl.classList.add(tooltip.yAlign);
    					} else {
    						tooltipEl.classList.add('no-transform');
    					}

    					function getBody(bodyItem) {
    						return bodyItem.lines;
    					}

    					// Set Text
    					if (tooltip.body) {
    						var titleLines = tooltip.title || [];
    						var bodyLines = tooltip.body.map(getBody);
    						var innerHtml = '<thead>';

    						titleLines.forEach(function(title) {
    							innerHtml += '<tr><th>' + title + '</th></tr>';
    						});
    						innerHtml += '</thead><tbody>';
    						
    						bodyLines.forEach(function(body, i) {
    							var colors = tooltip.labelColors[i];
    							var style = 'background:' +  rgb2hex(colors.backgroundColor);
    							style += '; border-color:' +  rgb2hex(colors.borderColor);
    							style += '; border-width: 2px'; 
    							var span = '<span class="chartjs-tooltip-key" style="' + style + '"></span>';
    							innerHtml += '<tr><td>' + span + body + '</td></tr>';
    						});
    						innerHtml += '</tbody>';

    						var tableRoot = tooltipEl.querySelector('table');
    						tableRoot.innerHTML = innerHtml;
    					}

    					var position = this._chart.canvas.getBoundingClientRect();

    					// Display, position, and set styles for font
    					tooltipEl.style.opacity = 5;
    					tooltipEl.style.left = tooltip.caretX + 'px';
    					tooltipEl.style.top = tooltip.caretY + 'px';
    					tooltipEl.style.fontFamily = tooltip._fontFamily;
    					tooltipEl.style.fontSize = tooltip.fontSize;
    					tooltipEl.style.fontStyle = tooltip._fontStyle;
    					tooltipEl.style.padding = tooltip.yPadding + 'px ' + tooltip.xPadding + 'px';
    				},
    				callbacks: {
	                       label: function(tooltipItem, data) {
	                           var dataset = data.datasets[tooltipItem.datasetIndex];
	                         var total = dataset.data.reduce(function(previousValue, currentValue, currentIndex, array) {
	                           return previousValue + currentValue;
	                         });
	                         var currentValue = dataset.data[tooltipItem.index];
	                         var percentage = Math.floor(((currentValue/total) * 100)+0.5);         
	                         return data.labels[tooltipItem.index] + " : " + percentage + "% (" + currentValue + ")";
	                       }
	                   }

    			}
    		}
    	});   
 	} 	
}
function chartBar(data1, label1, id, transTooltip, formatDate, data2, label2){
	if(typeof document.getElementById(id) != "undefined"){
    	var listLabels = [];
    	var listValeurs = [];
    	var listTooltipLabels = [];
    	for (var idx in data1){ 
    		if(formatDate){
    			idx = formatDateString(idx);
    		}
    		listTooltipLabels.push(idx);
    		if (idx.length > 12) {
    			idx = idx.substring(0, 12) +".";
    		}
        	if(transTooltip != null){
        		b = idx.replace(/_/g,"");
        		msg = transTooltip + b.charAt(0).toUpperCase() + b.slice(1).toLowerCase(); 
        		idx = messages[msg];
        	}
    		listLabels.push(idx);
    		listValeurs.push(data1[idx]);
        };
    	var datasets = [{
	            label: label1,
    			backgroundColor: generateColors[3],
	            borderColor: generateBorderColors[3],
	            borderWidth: 1,
	            data : listValeurs
    	}];
    	if(data2 !=null){
    		var listValeurs2 = [];
    		for (var idx2 in data2){ 
    			listValeurs2.push(data2[idx2]);
    		};
    		datasets.push({
	            label: label2,
    			backgroundColor: generateColors[4],
	            borderColor: generateBorderColors[4],
	            borderWidth: 1,
    			data : listValeurs2
    	})
    	}
        var  barChartData = {
            	labels : listLabels,
            	datasets : datasets
            }
     	var ctx = document.getElementById(id).getContext("2d");
    	myBar = new Chart(ctx, {
    		type: 'bar',
    		data: barChartData,
    		options: {responsive : true,
    			legend: {
    				display: false
    			},
    			tooltips: {
    				bodyFontSize : 22,
    				callbacks: {
                        title: function (t, e) {
                        	tootipTitle = listTooltipLabels[t[0].index];
                        	if(transTooltip != null){
                        		b = tootipTitle.replace(/_/g,"");
                        		msg = transTooltip + b.charAt(0).toUpperCase() + b.slice(1).toLowerCase(); 
                        		tootipTitle = messages[msg];
                        	}
                        	return tootipTitle;
                        }
                	}
    			},
    	        scales: {
    	            yAxes: [{
    	                ticks: {
    	                    beginAtZero:true
    	                }
    	            }]
    	        }
    		}
    	});
	}	
}
function lineChart(data, id, fill, arrayDates, byMonth, formatDate){
 	if(typeof document.getElementById(id) != "undefined"){
    	var inlineValeurs = [];
    	var inlineDatasets = [];
    	var a=0;
    	//dates
    	var xValues = [];
    	var dates = arrayDates;
    	//Hack : on enlève le '_'
    	for (var idx in data){ 
			if(!byMonth){			
				xValues= Object.keys(data[idx]).filter(function (propertyName) {
				    return propertyName.substr(1, propertyName.length);
				});
				for (var ind in dates){
					if(xValues.indexOf(dates[ind])>-1){
		        		inlineValeurs.push(data[idx][dates[ind]]);
	    			}else{
		        		inlineValeurs.push(null);
	    			}	
				};
			}else{
				var keyMois = Object.keys(data[idx]);
				for(i=1;i<=12;i++){
					if(keyMois.indexOf(i.toString())>-1){
		        		inlineValeurs.push(data[idx][i]);
	    			}else{
		        		inlineValeurs.push(null);
	    			}			
				}
			}
            inlineDatasets.push({
            	//Hack : on enlève le '_'
        	     label: idx.substr(1, idx.length),
		         backgroundColor: generateColors[a],
		         borderColor: generateColors[a],
		         pointColor: generateBorderColors[a],
		         pointBorderColor: "#fff",
		         pointHoverBorderColor: "#fff",
		         pointBackgroundColor: generateColors[a],
	             data: inlineValeurs,
	             spanGaps: true,
	             fill : fill
            });a++;inlineValeurs = [];
        };
		var dateLabels = dates;
		if(formatDate){
			dateLabels = [];
			for (var ind in dates){
				dateLabels.push(formatDateString(dates[ind]));
			};
		}
     	var dataMois = {
 		    labels: dateLabels,
 		    datasets: inlineDatasets
 		};      
     	var ctx3 = document.getElementById(id).getContext("2d");
     	var myLineChart = new Chart(ctx3, {
    		type: 'line',
    		data: dataMois,
    		options: {
    			responsive: true,
    			 scales:{
    	                xAxes:[{
    	        			ticks: {
    	        				autoSkip: true
    	        			}
    	                }]
    	            },
	                tooltips: {
	                	mode: 'label',
	                	titleFontSize: 14,
	                	bodyFontSize: 25
	                }
    		}
    	});     
 	}
}

//==jQuery document.ready
document.addEventListener('DOMContentLoaded', function() {
	//Autocomplete
	if (typeof searchEppnUrl != "undefined") {
		 var searchEppn = document.getElementById("searchEppn");
		 var searchEppnForm =  document.getElementById("searchEppnForm");
		 if(searchEppn != null){
			 searchEppnAutocomplete("searchEppn", searchEppnUrl, "eppn");
			
			 searchEppn.addEventListener("awesomplete-selectcomplete", function(){
				 searchEppnForm.submit();
			 });
		 }
		 var searchEppnTemp = document.getElementById("searchEppnTemp");
		 if(searchEppnTemp != null){
			 searchEppnAutocomplete("searchEppnTemp", searchEppnUrl, "eppn");
			 searchEppnTemp.addEventListener("awesomplete-selectcomplete", function(){
				 searchEppnAction("searchEppnTemp");
			 });
			 searchEppnTemp.addEventListener("keydown", function(event){
				if (event.keyCode == 13) {
					searchEppnAction("searchEppnTemp");
					event.preventDefault();
				}
			 });
		 }
	}
    if (typeof searchLdapUrl != "undefined") {
    	searchEppnAutocomplete("searchLdap", searchLdapUrl, "sn");
		var searchLdap = document.getElementById("searchLdap");
		var searchLdapForm =  document.getElementById("searchLdapForm");
		searchLdap.addEventListener("awesomplete-selectcomplete", function(event){
				var splitEppn = this.value.split("//");
				searchLdap.value = splitEppn[0].toString().trim();
				searchLdapForm.submit();
		 });
    }
    
    //stats
    if(typeof (statsRootUrl) != "undefined"){
    	//getStats(id, chartType, selectedType, spinner, option, transTooltip, formatDate, label1, data2, label2, fill, arrayDates, byMonth)
    	getStats("cardsByYearEtat", "multiBar", selectedType, 0);
    	getStats("crous", "pie", selectedType, 1);
    	getStats("difPhoto", "pie", selectedType, 2);
    	getStats("cardsByDay", "chartBar", selectedType, 3);
    	getStats("paybox", "multiBar", selectedType, 4);
    	getStats("motifs", "chartBar", selectedType, 5);
    	getStats("dates", "lineChart", selectedType, 6, null, null, false, null, null, null, true, monthsArray, true);
    	getStats("deliveredCardsByDay", "chartBar", selectedType, 7);
    	getStats("encodedCardsByday", "chartBar", selectedType, 8);
    	getStats("nbCards", "doughnut", selectedType, 9);
    	getStats("editable", "chartBar", selectedType, 10);
    	getStats("verso5", "doughnut", selectedType, 11);
    	getStats("browsers", "pie", selectedType, 12);
    	getStats("os", "pie", selectedType, 13);
    	getStats("nbRejets", "doughnut", selectedType, 14);
    	getStats("notDelivered", "chartBar", selectedType, 15);
    	getStats("cardsMajByDay", "multiBar", selectedType, 16);
    	getStats("cardsMajByIp", "doughnut", selectedType, 17);
    	getStats("lineCardsMajByDay", "lineChart", selectedType, 18, null, null, false, null, null, null, false, oneMonthBefore, false);
    	getStats("deliveryByAdress", "pie", selectedType, 19, "legend");
    	getStats("userDeliveries", "chartBar", selectedType, 20);
    	getStats("tarifsCrousBars", "multiBar", selectedType, 21);
    	getStats("cardsByMonth", "chartBar", selectedType, 22, null, null, null, "Demandes", "encodedCardsByMonth", "Carte encodées");
    	getStats("nbRejetsByMonth", "chartBar", selectedType, 23);
    	getStats("requestFree", "multiBar", selectedType, 24);
    	getStats("templateCards", "doughnut", selectedType, 25);
    }
})
