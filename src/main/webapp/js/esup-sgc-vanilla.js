//Turn alert js in bootstrap style
window.alert = function(message) {
	var blankTemplate ='<div id="modalID" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">'
		 + '<div class="modal-dialog"> <div class="modal-content">'
		 + '</div></div></div>';

		document.getElementsByTagName("body")[0].insertAdjacentHTML('beforeend', blankTemplate);
    	message = message != "undefined" ? message : "";
    	var myModal = document.getElementById('modalID');
    	var myModalInstance = new Modal(myModal, 
    	{ // options object
    	  content: '<div class="modal-header" style="min-height:40px;">\
    	      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>\
    	      <h4 class="modal-title"></h4>\
    	  </div><div class="modal-body"><div class="alert alert-warning alert-dismissible" role="alert"><strong>' + message + '</strong></div></div>', // sets modal content
    	  backdrop: 'static', // we don't want to dismiss Modal when Modal or backdrop is the click event target
    	  keyboard: false // we don't want to dismiss Modal on pressing Esc key
    	});

    	myModalInstance.show();
};

var photoExportZoom = 2;
var suneditor = null;

//Configs
function createSunEditor(){
  	SUNEDITOR.create('valeur',{
   		 // new CSS font properties
   	addFont: null,

   	  // height/width of the editor
   	width : '100%',
    height : '150px',

   	  // show/hide toolbar icons
   	 buttonList : [
         ['undo', 'redo'],
         ['font', 'fontSize', 'formats'],
         ['bold', 'underline', 'italic', 'strike', 'removeFormat'],
         ['fontColor', 'hiliteColor'],
         ['indent', 'outdent'],
         ['align', 'line', 'list', 'table'],
         ['link'],
         ['fullScreen', 'codeView']
     ]
   	});	
}

function displayFormconfig(val,col,valeur){
	var checkTrue = "";
	var checkFalse = "";
	if(valeur == "true"){
		checkTrue = "checked='checked'";
	}else if(valeur == "false"){
		checkFalse = "checked='checked'";
	}
    if(checkTrue == "" && checkFalse == "" && val=="BOOLEAN"){
    	checkTrue = "checked='checked'";
    }
	var bool = '<div id="boolGroup"><label class="radio-inline"><input type="radio" name="value" id="boolTrue" value="true"' + checkTrue +'/> True'
				+'</label><label class="radio-inline"><input type="radio" name="value" id="boolFalse" value="false" ' + checkFalse +' /> False</label></div>';

	var areaEditor = document.getElementById("areaEditor");
	
    switch(val) {
	    case 'HTML':
	   	 	remove("boolGroup");
	   	 	remove("valeur");
	   	 	remove("suneditor_valeur");
	   	 	areaEditor.insertAdjacentHTML('afterend', "<div class='col-md-" + col +"'><textarea class='form-control' id='valeur' name='value'>" + valeur + "</textarea></div>");
	   	 	suneditor = createSunEditor();
	        break;
	    case 'TEXT':
	   	 	if(suneditor != null){
	   	 		suneditor = suneditor.hide();
	   	 	}
	   	 	remove("suneditor_valeur");
	   	 	remove("boolGroup");
	   	 	remove("valeur");
	   	 	areaEditor.insertAdjacentHTML('afterend', "<div class='col-md-" + col +"'><textarea class='form-control' id='valeur' name='value'>" + valeur + "</textarea></div>");
	        break;
	    case 'BOOLEAN':
	   	 	if(suneditor != null){
	   	 		suneditor = suneditor.hide();
	   	 	}
	   	 	remove("suneditor_valeur");
	   	 	remove("valeur");
	   	 	areaEditor.insertAdjacentHTML('afterend', bool);
	        break;
    }
}
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
//==jQuery empty()
function empty(id) {
	var wrap = document.getElementById(id);
	if(wrap != null){
		while (wrap.firstChild) wrap.removeChild(wrap.firstChild);
	}
}
//insert before
function insertBefore(el, referenceNode) {
    referenceNode.parentNode.insertBefore(el, referenceNode);
}
//Webcam
function set_webcam() {
	    Webcam.set({
	  	  width: 320,
	  	  height: 240,
	  	  dest_width: 640,
	  	  dest_height: 480,

	  	  image_format: 'jpeg',
	  	  jpeg_quality: 90,
	  	  upload_name: "webcam"
	  	 });
	  	 Webcam.attach( 'my_camera' );
}

//dates array
function getDatesInrange(d1, d2, interval){
var dates = [];
while(+d1 < +d2){
  dates.push(formateDate(d1));
  d1.setDate(d1.getDate() + interval)
}
return dates.slice(0)
}

function formateDate(date){
return  [date.getFullYear(),
        getDoubleDigits(date.getMonth() +1),getDoubleDigits(date.getDate())
        ].join('-')
}

var startDate = new Date();
var endDate = new Date()
//1mois avant
startDate.setMonth(endDate.getMonth() -1);
var oneMonthBefore =getDatesInrange(startDate, endDate, 1)
endDate.setDate(endDate.getDate());
function getDoubleDigits(str) {
return ("00" + str).slice(-2);
}
var monthsArray = ["Jan", "Fev", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"];

//Retouche image , filtres --> Konva
function loadImages(sources, callback) {
    var images = {};
    var loadedImages = 0;
    var numImages = 0;
    for(var src in sources) {
      numImages++;
    }
    for(var src in sources) {
      images[src] = new Image();
      images[src].onload = function() {
        if(++loadedImages >= numImages) {
          callback(images);
        }
      };
      images[src].src = sources[src];
    }
  }
  function buildStage(images) {
    var stage = new Konva.Stage({
      container: 'container',
      width: 300,
      height: 376
    });
    var layer = new Konva.Layer();
    var lion = new Konva.Image({
      image: images.lion,
      x: 150,
      y: 188,
      width: 300,
      height: 376,
      draggable: true,
      offset: {
          x: 150,
          y: 188
      }
    });
    lion.cache();
    lion.filters([Konva.Filters.Brighten, Konva.Filters.Enhance, Konva.Filters.Contrast]);
    layer.add(lion);
    stage.add(layer);
    //Brighten
    var slider = document.getElementById('brighten');
    function sliderChange() {
    	lion.brightness(slider.value);
    	layer.batchDraw();   
    }
    slider.addEventListener('input', sliderChange);
    //Enhance 
    var slider1 = document.getElementById('enhance');
    function sliderChange1() {
        lion.enhance(slider1.value);
        layer.batchDraw(); 
    }
    slider1.addEventListener('input', sliderChange1);
    //Rotation
    var slider2 = document.getElementById('rotate'); 
    function sliderChange2() {
    	degrees = this.value;
    	lion.setRotation(degrees);
    	lion.rotate(degrees*Math.PI/180);
      	layer.draw();
    }
    slider2.addEventListener('input', sliderChange2);
    //Contraste
    var slider3 = document.getElementById('contrast');
    function sliderChange3() {
          lion['contrast'](parseFloat(slider3.value));
          layer.batchDraw();
    }
    slider3.addEventListener('input', sliderChange3);
  }
  
//Freefield select
function displayResultFreefield(selectedField, fieldsValue, indice){
	if(typeof freeUrl != "undefined"){
		var request = new XMLHttpRequest();
		request.open('GET', freeUrl + "?field=" + selectedField, true);
		request.onload = function() {
		  if (request.status >= 200 && request.status < 400) {
			  try {
				  var data = JSON.parse(this.response);
				  empty("freeFieldValue" + indice);
				  var freeFieldValue = document.getElementById("freeFieldValue" + indice); 
				  if(data.length>1){
						freeFieldValue.insertAdjacentHTML('beforeend',"<option value='' data-placeholder='true'></option>");  
				  }
				  for(let key in data){
					  var selected = "";
			      	  var fieldsValue4thisField = fieldsValue.split(';')[indice];
				      if(typeof fieldsValue4thisField != "undefined" && fieldsValue4thisField.split(",").includes(key.substring(1, key.length))){
		        		selected ="selected='selected'";
				      }
			      	  freeFieldValue.insertAdjacentHTML('beforeend',"<option value='"+ key.substring(1, key.length) + "' " + selected +  ">" + data[key] + "</option>");  
		          };            
			  } catch(e){
				  console.log(e);
			  }
		  }
		};
		request.send();
	}
}
  
//Traitement par lot des cartes
function multiUpdateForm(idArray) {
	if(typeof multiUpdateFormUrl != "undefined"){
		var request = new XMLHttpRequest();
		request.open('POST', multiUpdateFormUrl + "?cardIds=" + idArray.toString(), true);
		request.onload = function() {
		  if (request.status >= 200 && request.status < 400) {
			  document.getElementById("traitementLot").innerHTML = this.response;
			  //Bouton caché!!
			  var collapseLink = document.getElementById('hiddenBtn');
			  var myCollapseInit = new Collapse(collapseLink);
			  var forcedForm = document.getElementById("forcedForm");
			    if(forcedForm != null){
			    	forcedForm.addEventListener('submit', function(evt) {
			    		if(!document.getElementById("checkAdmin").checked){
			    			alert (messages['alertForcedEtat']);
			    			evt.preventDefault();
			    		}else if(document.getElementById("forcedEtatFinal").value == ""){
			    			console.log("tttt");
			    			alert (messages['alertForcedEtat']);
			    			evt.preventDefault();
			    		}else{
			    	        if (confirm("Confirmer cette action de changement d'état")) {
		    		           return true;
		    		        }else{
		    		        	evt.preventDefault();
		    		        }
			    		}
			     	}); 
			    }
			    //Renouvellement
			    var renewedForm = document.getElementById("RENEWEDForm");
				if (renewedForm != null) {
					renewedForm.addEventListener('submit', function(evt) {
						evt.preventDefault();
						if (confirm("Confirmer l'action de renouvellement")) {
							renewedForm.submit();
						}
					});
				}
			  //Actions
			  var inprintForm = document.getElementById("IN_PRINTForm");
			  if(inprintForm != null){
				  inprintForm.addEventListener('submit', function(e) {
			   	    	window.open('', 'formprint', 'width=800,height=600,resizeable,scrollbars,menubar');
			   	    	this.target = 'formprint';
			  	  }); 
			  }else if(document.getElementById("REJECTEDForm") !=null){
					var myButton = document.getElementById('REJECTEDBtn');
					var myModalInstance = new Modal(myButton); 
			  }else if(document.getElementById("ENABLEDForm") !=null){
					var myButton = document.getElementById('ENABLEDBtn');
					var myModalInstance = new Modal(myButton); 
			  }else if(document.getElementById("DISABLEDForm") !=null){
					var myButton = document.getElementById('DISABLEDBtn');
					var myModalInstance = new Modal(myButton); 
			  }
			  if(document.getElementById("validationBtn") !=null){
					var myButton2 = document.getElementById('validationBtn');
					var myModalInstance2 = new Modal(myButton2); 
			  }
			  // Choix message
			  if(document.getElementById("editActions") !=null){
					var editActions = document.getElementById('editActions');
					editActions.querySelectorAll("a.accordion-toggle").forEach(function(collapseLink) {
						var myCollapseInit = new Collapse(collapseLink);
				    }, false); 
			  }
		  }
		};
		request.send();
	}
};
//Preview image d'un file input
function previewFileInput(idInputFile, targetImg, preview) {
	if(document.getElementById(idInputFile)!=null){
		document.getElementById(idInputFile).addEventListener("change", function () {
	        if (typeof (FileReader) != "undefined") {
	            var image_holder = document.getElementById(targetImg);
	            empty(targetImg);
	            var reader = new FileReader();
	            reader.onload = function (e) {
	                image_holder.insertAdjacentHTML('beforeend',"<img src='" + e.target.result + "' class = 'thumb-image'/>");  
	                //Live preview
	                if(preview == "masque"){
	                	var mask = document.getElementById("specimenCarte");
	                	if(mask != null){
	                		mask.style.backgroundImage = 'url(' + e.target.result + ')'; 
	                	}
	                }else{
	                	var notMask = document.getElementById(preview);
	                	if(notMask != null){
	                		notMask.getElementById(preview).setAttribute("src", e.target.result);
	                	}
	                }
	            }
	            image_holder.style.display = '';
	            reader.readAsDataURL(this.files[0]);
	        } else {
	            alert("This browser does not support FileReader.");
	        }
	    });
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
function searchEppnAutocomplete(id, url, field, param2) {
	var searchBox = document.getElementById(id);
	if(searchBox != null){
	 	var awesomplete = new Awesomplete(searchBox, {
	 		  minChars: 3,
	 		  maxItems: 10
	 		});
	 	searchBox.addEventListener("keyup", function(){
			if(this.value.length>2) {
				if(param2 == "ldapTemplateName"){
					var ldapTemplateName = document.getElementById("ldapTemplateName");
					var param2Url =  "&ldapTemplateName=" +  ldapTemplateName.value;
				}else{
					var param2Url = "";
				}
				var request = new XMLHttpRequest();
				request.open('GET', url + "?searchString=" + this.value + param2Url, true);
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
	if(c!=null){
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
	}
};

//affiche stats
function getStats(id, chartType, dateFin, selectedType, spinner, option, transTooltip, formatDate, label1, data2, label2, fill, arrayDates, byMonth){
	
	var prefId = document.getElementById(id);
	if((prefsStatsRm!=null && prefsStatsRm !="" && !(prefsStatsRm.indexOf(prefId)>-1)) || (prefsStatsRm==null || prefsStatsRm =="")){
		displaySpinner(document.querySelector("canvas#" + id), spinner);
		var request = new XMLHttpRequest();
		request.open('GET',  statsRootUrl + "/json?type=" + id + "&typeInd=" + selectedType + "&anneeUniv=" + anneeUniv.value, true);
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
	if(document.getElementById(id) != null){
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
    if(document.getElementById(id) != null){
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
	if(document.getElementById(id) != null){
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
 	if(document.getElementById(id) != null){
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
			 searchEppnAutocomplete("searchEppn", searchEppnUrl, "eppn", "");
			
			 searchEppn.addEventListener("awesomplete-selectcomplete", function(){
				 searchEppnForm.submit();
			 });
		 }
		 var searchEppnTemp = document.getElementById("searchEppnTemp");
		 if(searchEppnTemp != null){
			 searchEppnAutocomplete("searchEppnTemp", searchEppnUrl, "eppn", "");
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
    	searchEppnAutocomplete("searchLdap", searchLdapUrl, "sn", "ldapTemplateName");
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
    	//getStats(id, chartType, dateFin, selectedType, spinner, option, transTooltip, formatDate, label1, data2, label2, fill, arrayDates, byMonth)
    	function loadStats(anneeUniv, selectedType,monthsArray){
	    	getStats("cardsByYearEtat", "multiBar", anneeUniv, selectedType, 0);
	    	getStats("crous", "pie", anneeUniv, selectedType, 1);
	    	getStats("difPhoto", "pie", anneeUniv, selectedType, 2);
	    	getStats("cardsByDay", "chartBar", anneeUniv, selectedType, 3);
	    	getStats("paybox", "multiBar", anneeUniv, selectedType, 4);
	    	getStats("motifs", "chartBar", anneeUniv, selectedType, 5);
	    	getStats("dates", "lineChart", anneeUniv, selectedType, 6, null, null, false, null, null, null, true, monthsArray, true);
	    	getStats("deliveredCardsByDay", "chartBar", anneeUniv, selectedType, 7);
	    	getStats("encodedCardsByday", "chartBar", anneeUniv, selectedType, 8);
	    	getStats("nbCards", "doughnut", anneeUniv, selectedType, 9);
	    	getStats("editable", "chartBar", anneeUniv, selectedType, 10);
	    	getStats("browsers", "pie", anneeUniv, selectedType, 12);
	    	getStats("os", "pie", anneeUniv, selectedType, 13);
	    	getStats("nbRejets", "doughnut", anneeUniv, selectedType, 14);
	    	getStats("notDelivered", "chartBar", anneeUniv, selectedType, 15);
	    	getStats("deliveryByAdress", "pie", anneeUniv, selectedType, 19, "legend");
	    	getStats("userDeliveries", "chartBar", anneeUniv, selectedType, 20);
	    	getStats("tarifsCrousBars", "multiBar", anneeUniv, selectedType, 21);
	    	getStats("cardsByMonth", "chartBar", anneeUniv, selectedType, 22, null, null, null, "Demandes", "encodedCardsByMonth", "Carte encodées");
	    	getStats("nbRejetsByMonth", "chartBar", anneeUniv, selectedType, 23);
	    	getStats("requestFree", "multiBar", anneeUniv, selectedType, 24);
	    	getStats("templateCards", "doughnut", anneeUniv, selectedType, 25);
	    	getStats("europeanCardChart", "pie", anneeUniv, selectedType, 26);
	    	getStats("nbRoles", "doughnut", anneeUniv, selectedType, 27);
    	}
    	var anneeUniv = document.getElementById("anneeUniv");
    	
    	new SlimSelect({
	    	  select: '#anneeUniv',
	    	  showSearch: false
	    	})
    	anneeUniv.addEventListener("change", function(event){
    		loadStats(anneeUniv, selectedType, monthsArray);
    	});
    	loadStats(anneeUniv, selectedType, monthsArray);

       	var downloadBtn = document.querySelectorAll('.downloadBtn');
    	Array.from(downloadBtn).forEach(function(link) {
    		return link.addEventListener('click', function(event) {
    			this.href = this.title + "/" + anneeUniv.value;
    	    });
    	});
    }
    
	//preview template
	previewFileInput("templateMasque", "image-holder-masque", "masque");
	previewFileInput("templateLogo", "image-holder-logo","logo-ur");
	previewFileInput("templateQrCode", "image-holder-qrCode", "qrcode");
	
   	//Traitement par lot  cartes
  	var idArray = [];
   	var currentEtat = "";
   	var checkboxes = document.querySelectorAll('.case');
   	var selectAll = document.getElementById("selectall");
   	var listeIds = document.getElementById("listeIds");
   	if(selectAll != null){
	   	selectAll.addEventListener("click", function(){
			idArray = [];
			for(var i=0, n=checkboxes.length;i<n;i++){
				checkboxes[i].checked = this.checked;
			}
	
			const values = Array.from(document.querySelectorAll('input[type="checkbox"]')).
			filter(function(checkbox) {
				  return checkbox.checked;
			}).map(function(checkbox) {
				  return checkbox.value;
			});
			for(var i=0, n=checkboxes.length;i<n;i++){
				checkboxes[i].checked = this.checked;
			}
			idArray = Array.from(document.querySelectorAll("input[name='case']")).filter(function(checkbox) {
				  return checkbox.checked;
			}).map(function(checkbox) {
				  return checkbox.value;
			});
			multiUpdateForm(idArray);
			if(listeIds != null){
				listeIds.innerHTML = idArray;
			}
		});
	
	   	Array.from(checkboxes).forEach(function(link) {
	   		return link.addEventListener('click', function(event) {
	   			if(idArray.indexOf(this.value)< 0){
	   				idArray.push(this.value);
	   			}else{
	   				idArray.splice(idArray.indexOf(this.value), 1 );
	   			}
	   	    	if(!this.checked){
	   	    		selectAll.checked = false;
	   	    	}		
	   			multiUpdateForm(idArray);
				if(listeIds != null){
					listeIds.innerHTML = idArray;
				}
	   	    }); 
	   	});
	
	   	window.onload = function(e){ 
			selectAll.checked = false;
			for(var i=0, n=checkboxes.length;i<n;i++){
				checkboxes[i].checked = false;
			}
		};
   	}
	//Reset filtres
	var nofilter = document.getElementById("nofilter");
		if(nofilter != null){
		nofilter.addEventListener("click", function(){
			window.location = rootManagerUrl + "?index=first";
		});
	}
	//admin sessions
	if(document.getElementById("sessionsUsers")!=null){
		insertBefore(document.getElementById("sessionsdiv"), document.getElementById("sessionsUsers"))
	}
	
	//choix champs import csv recherche
	var selectAllFields = document.getElementById("selectAllFields");
	if(selectAllFields!=null){
		var caseFields = document.querySelectorAll('.caseField');
		selectAllFields.addEventListener("click", function () {
			for(var i=0, n=caseFields.length;i<n;i++){
				caseFields[i].checked = this.checked;
			}			
		});
		
	   	Array.from(caseFields).forEach(function(link) {
	   		return link.addEventListener('click', function(event) {
	   	    	if(!this.checked){
	   	    		selectAllFields.checked = false;
	   	    	}		
	   	    });
	   	});
	}
	
    //Onglets Cartes
	var tabsCard = document.querySelectorAll('#cardList .nav-tabs li a');
	if(tabsCard != null){
		var searchEppnForm =  document.getElementById("searchEppnForm");
		for (var i = 0; i < tabsCard.length; i++) {
		    result = tabsCard[i];
		    result.addEventListener('click', function() {
		    	var type = this.hash
		    	type = type.substr(1,3);
		    	searchEppnForm.querySelectorAll("select").forEach(function(element) {
					element.disabled = true;
				});
		    	document.getElementById("searchEppn").disabled = true;
		    	document.getElementById("searchBeanType").value = type;
		    	document.getElementById("searchBeanType").disabled = false;
		    	searchEppnForm.submit();
		    });
		}
	}
	
	/* clear form to have only select used */
	var searchEppnForm =  document.getElementById("searchEppnForm");
	if(searchEppnForm!=null) {
		searchEppnForm.addEventListener("submit", function() {
			searchEppnForm.querySelectorAll("select").forEach(function(element) {
				 if(!element.value) {
					 element.disabled = true;
				 }
			});
			searchEppnForm.submit();
	    }, false); 
	}
	
    //Tabs Stats Leo
	var tabsStats = document.querySelectorAll('#statsManager .nav-tabs li a');
	if(tabsStats != null){
		for (var i = 0; i < tabsStats.length; i++) {
		    result = tabsStats[i];
		    result.addEventListener('click', function() {
		    	var type = this.hash
		    	type = type.substr(1,3);
		    	if(type == 0){
		        	if(typeof statsRootUrl != "undefined"){
		        		window.location.href = 	statsRootUrl;
		        	};	
		    	}else{
			    	if(typeof tabsUrl != "undefined"){
			    		window.location.href = 	tabsUrl + type;
			    	};
		    	}
		    });
		}
	}
	
    //Formulaire paybox
    if(typeof displayPayboxForm != "undefined"){
	    if(displayPayboxForm){
	    	document.getElementById("payboxForm").submit();
	    }
    }
    //Logs submit
    var logsList = document.querySelectorAll('#logsList select');
    if(logsList != null){
		for (var i = 0; i < logsList.length; i++) {
		    result = logsList[i];
		    result.addEventListener('change', function() {
		    	this.form.submit();
		    });
		}
	}
    
    //Mise en page libre stats
    if (typeof statsRootUrl != "undefined") {
	    dragula([document.getElementById('statsPanels')]).on('dragend', dragend);
	    
	    function dragend (el) {
	    	var statsArray =[];
	    	var statsDrag = document.querySelectorAll('.statsDrag');
	    	if(statsDrag != null){
	    		for(var src in statsDrag) {
	    			statsArray.push(statsDrag[src].id);
	    		}
	    	}
	    	setPrefStats(statsArray, "STATS");
	   	}
	
	    function setPrefStats(statsArray, key) {
			var request = new XMLHttpRequest();
			request.open('POST', statsRootUrl + "/prefs?values=" + statsArray.toString() + "&key=" + key, true);
			request.send();
	    }
	    
	    if(prefsStats!=null && prefsStats !=""){
	    	for(var idx in prefsStatsRm) {
    			remove(prefsStatsRm[idx]);
    		}
	    	prefsStats.forEach(function (value, index) {
	    		var panel = document.getElementById(value);
	    		if(panel !=null){
	    			document.getElementById("statsPanels").append(panel);
	    		}
	        });
	    }
	    
	    //Remove stats
		var removeClass = document.querySelectorAll('.remove');
		for (var i = 0; i < removeClass.length; i++) {
		    result = removeClass[i];
		    result.addEventListener('click', function() {
			    prefsStatsRm.push(this.closest(".statsDrag").id);
		    	if(prefsStats==""){
			    	var statsArray =[];
			    	document.querySelectorAll('.statsDrag').forEach(function (element, index) {
			    		statsArray.push(element.id);
			    	});
			    	setPrefStats(statsArray, "STATS");
		    	}
		    	setPrefStats(prefsStatsRm, "STATSRM");
		    	this.closest(".statsDrag").remove();
		    	//refresh modal
		    	//$("#mainModal").load(location.href+" #mainModal>*","");
		    });
		};
    }
    var idSlimSelect = ['searchBeanAdress','searchBeanEtat','searchBeanEditable', 'searchBeanOwnOrFreeCard', 'searchBeanNbCards', 'searchBeanNbRejets', 'searchBeanFlagAdresse', 'searchBeanLastTemplateCardPrinted'];
    //Recherche simple
    idSlimSelect.forEach(function(element, i) {
    	if(document.getElementById(element) != null ){
    		var selectLabel = document.getElementById(element).previousSibling.innerText;
	    	var showSearch = false;
	    	if(element == 'searchBeanAdress'){
	    		showSearch = true;
	    	}
		    new SlimSelect({
		    	  select: '#' + element,
		    	  showSearch: showSearch,
		    	  placeholder: selectLabel,
		    	  allowDeselect: true
		    	})
    	}
    });
    //+ de filtres
    if(typeof nbFields != "undefined"){
	    for (var i = 0; i < nbFields; i++) {
	    	if(document.getElementById('fields' + i) != null ){
	    		var selectLabel = document.getElementById('fields' + i).previousSibling.innerText;
			    new SlimSelect({
			    	  select: '#fields' + i,
			    	  placeholder: selectLabel,
			    	  allowDeselect: true
			    	})
	    	}
		    if(document.getElementById('freeFieldValue' + i) != null ){
		    	selectLabel = document.getElementById('freeFieldValue' + i).previousSibling.innerText;
			    new SlimSelect({
			    	  select: '#freeFieldValue' + i,
			    	  placeholder: selectLabel,
			    	  allowDeselect: true
			    	})
		    }
	    }
    }
    
 	//Bouton imprimer bordereau
    var printPage = document.querySelectorAll('.printPage')
    if(printPage != null){
	   	Array.from(printPage).forEach(function(link) {
	   		return link.addEventListener('click', function(event) {
		   		 window.print();
				 return false;
	   	    });
	   	});
	}
    
    //Récupération des résultats du select champ libre
    if(typeof freeUrl != "undefined"){
    	var selectedField = document.querySelectorAll('.freeSelect');
    	selectedField.forEach(function (element, index) {
    		var indice = element.id.replace("fields","");
    		displayResultFreefield(element.value, fieldsValue, indice);
    	});
    	
        var freeSelect = document.querySelectorAll('.freeSelect')
        if(freeSelect != null){
    	   	Array.from(freeSelect).forEach(function(link) {
    	   		return link.addEventListener('change', function(event) {
    	    		var indice = link.id.replace("fields","");
    	    		selectedField = link.value;
    	    		displayResultFreefield(selectedField, fieldsValue, indice);
    	   	    });
    	   	});
    	}
    }
    //sélection adresse en fonction de l'état
    var searchBeanEtat = document.getElementById('searchBeanEtat');
    if(searchBeanEtat != null){
    	searchBeanEtat.addEventListener('change', function(event) {
			if(typeof filterAdressUrl != "undefined"){
				var searchBeanAdress = document.getElementById('searchBeanAdress');
	            var selectedTab = document.querySelector('#tabTypes .active a').hash
	            var tabActif = selectedTab.substr(1,3);
	    		var request = new XMLHttpRequest();
	    		request.open('GET', filterAdressUrl + "?etat=" + searchBeanEtat.value + "&tabType=" + tabActif, true);
	    		request.onload = function() {
	    		  if (request.status >= 200 && request.status < 400) {
	    			  try {
	    				  var data = JSON.parse(this.response);
							empty("searchBeanAdress");
							if(Object.keys(data).length>1){
								searchBeanAdress.insertAdjacentHTML('beforeend',"<option value='' data-placeholder='true'></option>");  
							}
							for(let key in data) {
				        		searchBeanAdress.insertAdjacentHTML('beforeend',"<option value='"+ key + "'>" + data[key] + "</option>");  
				        	};
	    			  }
	    			  catch(e){
	    				  console.log(e);
	    			  }
	    		  }
	    		};
	    		request.send();
			}
		});
    }
    
	//EZCROP
	var imageCropper = document.getElementById('image-cropper');
	if(imageCropper!=null){
		var ezcropPreview = document.getElementById('ezcrop-preview');
		var confirmPreview = document.getElementById('confirmPreview');
		var alertPreview = document.getElementById('alertPreview');
		var filterSliders = document.getElementById('filterSliders');
		var container = document.getElementById('container');
		var ezcropInfo = document.getElementById('ezcrop-info');
		var orientation = "1";
		//surcharge les valeurs par defaut de ezcrop.js
		exportDefaults = {
		        type: 'image/jpeg',
		        quality: 0.9,
		        originalSize: false,
		        //fillBg: '#fff'
		      };
		var cropper = new ezcrop(document.getElementById('image-cropper'), {
			onImageError : function(e) {
		    	if(e.code==1){
		    		alert(messages['photoTooSmall']);
		    		return false;
		    	}
			  },onFileChange: function(e) {
		        var file = e.target.files[0];
			    if(file.size > photoSizeMax){
				       alert(messages['photoTooBig'] + photoSizeMax/1000+ "Ko");
				     document.querySelectorAll(".ezcrop-image-input")[0].value = '';
				 }else{
			          if (file && file.name) {
			              EXIF.getData(file, function() {
			                  var exifData = EXIF.pretty(this);
			                  orientation = EXIF.getTag(file, "Orientation");
			              });
			          }
				 }
		      },previewSize:{
		    	  width: 150, height: 188},
		    	  maxZoom:1.8,
		    	  minZoom: 'fit'
	        });
		
		function changeMsg(){
			if(confirmPreview != null){
			    confirmPreview.value = "false";
			    alertPreview.classList.remove("alert-success");
			    alertPreview.classList.add("alert-danger");
			    alertPreview.innerHTML = "<span class='glyphicon glyphicon-warning-sign'><!----></span> " + messages['alertPreview'];
			}
		}
	
		if(typeof lastId != "undefined" && lastId != "-1" && isRejected.length ==0 && fromLdap=='false'){
			cropper.img.src = lastPhotoUrl;
		}
	
		
		ezcropPreview.addEventListener('dragover', function() {
			ezcropPreview.style.border = "2px dashed #ccc";
		});
		
	    document.getElementById('rotate-ccw').addEventListener('click', function() {
	        cropper.rotateCCW();
	        cropper.options.exportZoom = 2;
	        changeMsg();
	      });
	
		  document.getElementById('rotate-cw').addEventListener('click', function() {
		    cropper.rotateCW();
		    cropper.options.exportZoom = 2;
		    changeMsg();
		  });
		  
		  var imageInput = document.querySelectorAll('.ezcrop-image-input')
		  if(imageInput != null){
			Array.from(imageInput).forEach(function(link) {
		   		return link.addEventListener('change', function(event) {
				  cropper.options.exportZoom = 2;
				  changeMsg();
			    });
			});
		  }
		    
		  document.querySelectorAll(".ezcrop-image-zoom-input")[0].addEventListener('change', function() {
			  cropper.options.exportZoom = 2;
			  changeMsg();
		  });
		  if(document.getElementById('preview') != null){
			  document.getElementById('preview').addEventListener('click', function() {
		    	var currentImg =  document.querySelectorAll('.ezcrop-preview-image')[0];
		    	if(currentImg.src == ""){
		          	 alert(messages['fileRequired']);      	 
		          	 return false;
		        }
		    	currentImg.setAttribute("id","image-preview");
		    	var imageExif = document.getElementById('image-preview');
		    	//hack iphone,iPad
		    	if(isISmartPhone == "true" && orientation == "6"){
		    		cropper.rotateCW();
		    	}
		    	cropper.options.exportZoom = photoExportZoom;
		
		    	var image = cropper.getCroppedImageData();
		    	document.querySelector('#specimenCarte img#photo').setAttribute("src", image);
		    	document.querySelectorAll('.ezcrop-image-data')[0].value=image;
		    	//hack iphone,iPad
		    	if(isISmartPhone == "true" && orientation == "6"){
		    		cropper.rotateCCW();
		    	}
			 });
		 }
		 if(document.getElementById('confirmPhoto') != null){   
			 document.getElementById('confirmPhoto').addEventListener('click', function() {
		    	confirmPreview.value = "true";
			    alertPreview.classList.remove("alert-danger");
			    alertPreview.classList.add("alert-success");
			    alertPreview.innerHTML = "<span class='glyphicon glyphicon-ok'><!----></span> " + messages['confirmPreview'];
			    var previewCarte = document.getElementById('previewCarte');
			    if(previewCarte != null){
			    	 var myModalInstance = new Modal(previewCarte, null);
			    	 myModalInstance.hide();
			    }
			 });
		 }
		   
		//EZCROP RETOUCHE
			if(document.getElementById('btnRetouche') != null){
				document.getElementById('btnRetouche').addEventListener('click', function() {
					var sliders = document.getElementById('filterSliders');
					sliders.style.display = "none";
					var url = photorUrl +  document.querySelector("#retouche #cardId").value;
					cropper.img.src = url;
					cropper.previewSize = {width: 150, height: 188};
				});
			}
			
			if(document.getElementById('exportAdmin') != null){
				document.getElementById('exportAdmin').addEventListener('click', function() {
			    	if(document.querySelector("#container canvas")==null){
				    	var currentImg =  document.querySelectorAll('.ezcrop-preview-image')[0];
				    	if(currentImg.src == ""){
				          	 alert(messages['fileRequired']);      	 
				          	 return false;
				        }
				    	cropper.options.exportZoom = photoExportZoom;
				    	var image = cropper.getCroppedImageData();
				    	document.querySelectorAll('.ezcrop-image-data')[0].value=image;
			    	}
			       	else{
			       		canvas = document.getElementById("container").querySelector("canvas");
			       		var data64 = canvas.toDataURL('image/jpeg', 0.9);
			       		document.querySelectorAll('.ezcrop-image-data')[0].value=data64;
			       	}
			     });
			}		
			
			//KONVA
		    var img1 = new Image();
		    if(document.getElementById('btnColors') != null){
				document.getElementById('btnColors').addEventListener('click', function() {
				ezcropPreview.style.display = "none";
		    	filterSliders.style.display = "block";
		    	ezcropInfo.style.display = "none";
		    	container.style.display = "block";
		    	ezcropPreview.style.display = "none";
		    	var currentImg =  document.querySelectorAll('.ezcrop-preview-image')[0];
		    	if(currentImg.src == ""){
		          	 alert(messages['fileRequired']);      	 
		          	 return false;
		        }
		    	cropper.options.exportZoom = photoExportZoom;
		    	var image = cropper.getCroppedImageData();
		        var sources = {
		      	      lion: image
		      	    };
		      	loadImages(sources, buildStage);
		        var sliders = document.querySelectorAll('#filterSliders input');
		        if(sliders != null){
		    	   	Array.from(sliders).forEach(function(link) {
		    	   		return link.value = 0;
		    	   	});
		    	}
			    });
			}
		    
		    if(document.getElementById('resetBtn') != null){
				document.getElementById('resetBtn').addEventListener('click', function() {
					cropper.options.exportZoom = photoExportZoom;
					var url = photorUrl +  document.querySelector("#retouche #cardId").value;
					cropper.img.src = url;
			    	filterSliders.style.display = "none";
			    	ezcropInfo.style.display = "block";
			    	ezcropPreview.style.display = "block";
			    	empty("container"); 
			        var sliders = document.querySelectorAll('#filterSliders input');
			        if(sliders != null){
			    	   	Array.from(sliders).forEach(function(link) {
			    	   		return link.value = 0;
			    	   	});
			    	}
			        document.querySelectorAll(".ezcrop-image-zoom-input")[0].removeAttribute("disabled");
			    });
			}
		    if(document.getElementById('scaleBtn') != null){
				document.getElementById('scaleBtn').addEventListener('click', function() {	    
			    	canvas = document.getElementById("container").querySelector("canvas");
			    	if(canvas !=null){
				   		var data64 = canvas.toDataURL('image/jpeg', 0.9);
				   		cropper.img.src = data64;
				    	filterSliders.style.display = "none";
				    	ezcropInfo.style.display = "block";
				    	ezcropPreview.style.display = "block";
				    	empty("container"); 
			    	}
			    	document.querySelectorAll(".ezcrop-image-zoom-input")[0].removeAttribute("disabled");
			    });
			} 
	}
	//WEBCAM formulaire de demande
	var webcam = document.getElementById('webCam');
    //Si webcam détectée, la partie webcam est affichée
	 if(webcam != null){
		Webcam.on( 'error', function(err) {
			remove("webCam");
		} );
	    var camera = document.getElementById('my_camera');
	    if(typeof camera != "undefined"){
			Webcam.on( 'live', function() {
				// camera is live, showing preview image
				// (and user has allowed access)
				webcam.style.display = "block";
			} );
		    set_webcam();
		    document.getElementById('retryWebcam').addEventListener('click', function() {
		    	set_webcam();
		    	document.querySelectorAll('.ezcrop-preview-image')[0].removeAttribute('src');
		    	cropper.img.src = null;
		    });
		    document.getElementById('snapShot').addEventListener('click', function() {
		   	 Webcam.snap( function(data_uri) {
		   		cropper.img.src = data_uri;
		   		cropper.minZoom = 'fill';
		   	 } );
		    })
	    }
	 }
	 
	//Popover image Template interface
	var templateCardsListPopover = document.querySelectorAll('#templateCardsList .photo img');
	if(templateCardsListPopover != null){
		for (var i = 0; i < templateCardsListPopover.length; i++){
			new Popover(templateCardsListPopover[i], {
				  trigger: 'hover',
				  placement: 'top',
				  template: '<div class="popover" role="tooltip"><div class="popover-content"><img class="popTemplate" src="'+ templateCardsListPopover[i].src + '"/></div></div>'
				});
		}
	}
	 
    if(document.getElementById('_cssStyle_id') != null){
	    document.getElementById('_cssStyle_id').addEventListener('keyup', function() {
	    	remove("mainStyle");
	    	document.getElementById('specimenCarte').insertAdjacentHTML('beforeend','<style id="mainStyle">'+ this.value + '</style>');  
	    });
    }
    if(document.getElementById('_cssMobileStyle_id') != null){
    	document.getElementById('_cssMobileStyle_id').addEventListener('keyup', function() {
	    	remove("mobileStyle");
	    	document.getElementById('specimenCarte').insertAdjacentHTML('beforeend','<style id="mobileStyle">@media screen and (max-width: 450px) {'+ this.value + '}</style>');
	    });
    }
    if(document.getElementById('updateTemplateCard') != null){
    	document.querySelector('#updateTemplateCard #proceed').addEventListener('click', function() {
	        if (confirm('Enregistrer les modifications?')) {
	           return true;
	        }else{
	        	return false
	        }
	    });
    }
    
    //Validation formulaire demande de carte 
	var radio1 = document.getElementById("radio1");
	var radio2 = document.getElementById("radio2");
	var radio3 = document.getElementById("radio3");
	var radioCnil1= document.getElementById("radioCnil1");
	var radioCnil2 = document.getElementById("radioCnil2");
	var radioEurope1= document.getElementById("radioEurope1");
	var radioEurope2 = document.getElementById("radioEurope2");
	var reglement = document.getElementById("reglement");
	var adresse  = document.getElementById("address");
	if(document.getElementById('cardRequest') != null){
		document.getElementById('cardRequest').addEventListener('submit', function(e) {
	    	if(document.querySelectorAll('.ezcrop-image-data')[0].value == ""){
	    		alert (messages['alertPhoto']);
	    		e.preventDefault();
	    	}
	      	if(document.getElementById('confirmPreview').value == "false"){
	    		alert (messages['alertPreview']);
	    		e.preventDefault();
	    	}    
	     	if(radio1 != null && !radio1.checked && !radio2.checked){
	    		alert (messages['alertCrous']);
	    		e.preventDefault();
	    	}
	     	if(radioEurope1 != null && !radioEurope1.checked  && !radioEurope2.checked){
	    		alert (messages['alertEurope']);
	    		e.preventDefault();
	    	}      	
	     	if(radioCnil1!= null && !radioCnil1.checked && !radioCnil2.checked){
	    		alert (messages['alertCnil']);
	    		e.preventDefault();
	    	}   
	     	if(reglement!= null && !reglement.checked){
	    		alert (messages['alertRules']);
	    		e.preventDefault();
	    	}
	     	if(radio3 != null && radio3.checked && adresse != null && adresse.value.trim().length>250){
	    		alert (messages['alertTooChar']);
	    		e.preventDefault();
	     	}
	    });
	}
	
	/* CROUS RightHolder API View*/
	var crousRighHolder  = document.getElementById("getCrousRighHolder");
	if(crousRighHolder != null){
		crousRighHolder.addEventListener('click', function(e) {
		 	if (typeof getCrousRightHolderUrl != "undefined") {
				var request = new XMLHttpRequest();
				request.open('GET', getCrousRightHolderUrl, true);
				request.onload = function() {
				  if (request.status >= 200 && request.status < 400) {
					  var message = this.response;
						if (message && message.length) {
							document.getElementById('getCrousRighHolderDiv').insertAdjacentHTML('beforeend', message);
							crousRighHolder.setAttribute("disabled", true);
						}
				  }
				};
				request.send();
			}
		});
	}
	/* CROUS SmartCard API View*/
	document.querySelectorAll('.getCrousSmartCard').forEach(function(crousSmartCard) {
		crousSmartCard.addEventListener('click', function(e) {
			e.preventDefault();
			var request = new XMLHttpRequest();
			request.open('GET', this.href, true);
			var parent = this;
			request.onload = function() {
			  if (request.status >= 200 && request.status < 400) {
				  var message = this.response;
					if (message && message.length) {
						crousSmartCard.parentNode.nextSibling.innerHTML = message;
					}
			  }
			};
			request.send();
		});
	});
	
	/* ESCR Student API View*/
	var escrStudent  = document.getElementById("getEscrStudent");
	if(escrStudent != null){
		escrStudent.addEventListener('click', function(e) {
		 	if (typeof getEscrStudentUrl != "undefined") {
				var request = new XMLHttpRequest();
				request.open('GET', getEscrStudentUrl, true);
				request.onload = function() {
				  if (request.status >= 200 && request.status < 400) {
					  var message = this.response;
						if (message && message.length) {
							document.getElementById('getEscrStudentDiv').insertAdjacentHTML('beforeend', message);
							escrStudent.setAttribute("disabled", true);
						}
				  }
				};
				request.send();
			}
		});
	}
	/* ESCR Card API View*/
	document.querySelectorAll('.getEscrCard').forEach(function(escrCard) {
		escrCard.addEventListener('click', function(e) {
			e.preventDefault();
			var request = new XMLHttpRequest();
			request.open('GET', this.href, true);
			var parent = this;
			request.onload = function() {
			  if (request.status >= 200 && request.status < 400) {
				  var message = this.response;
					if (message && message.length) {
						escrCard.parentNode.nextSibling.innerHTML = message;
					}
			  }
			};
			request.send();
		});
	});
	
    //Cartes : sélection du message à envoyer
	document.addEventListener('click', function(e) {
	  if (e.target && /^msg_/.test(e.target.id)){
		var realSpan = e.target.id.replace("msg_", "span_");
		var comment = document.getElementById(realSpan).innerHTML.replace("&nbsp;","");
		document.getElementById(e.target.id.replace(/^msg((_\d)+)_(\d)/, "comment$1")).value = comment;
	  }
	})
	
    //Tabs Configs
	var tabsConfig = document.querySelectorAll('#configList .nav-tabs li a');
   	Array.from(tabsConfig).forEach(function(link) {
   		return link.addEventListener('click', function(event) {
	    	type = this.hash.substr(1,this.hash.length);
	    	if(type != 0){
		    	if(typeof tabsUrl != "undefined"){
		    		document.querySelector("input[name=searchField]").value = type;
		    		document.getElementById("tabConfigsForm").submit();
		    	};
	    	}else{
	    		window.location.href = 	configUrl;
	    	}
	    });
	});
   	
   	//configs
   	var appliConfig = document.getElementById('appliConfig');
   	if(appliConfig != null){ 
   		appliConfig.addEventListener('submit', function(e) {
   			document.getElementById("valeur").value = document.querySelectorAll(".input_editor")[0].contentDocument.body.innerHTML;
   		});
   	}

   	//CSV download
   	var downloadOk = document.getElementById('downloadOk');
   	if(downloadOk!=null){
	   	downloadOk.addEventListener('click', function(event) {
		    var modalFields = document.getElementById('modalFields');
		    if(modalFields != null){
		    	 var myModalInstance1 = new Modal(modalFields, null);
		    	 myModalInstance1.hide();
		    }
			document.getElementById("searchCsvForm").submit();
		});
   	}
   	
   	//Configs
   	var boolGroup = document.getElementById("boolGroup");
   	if(boolGroup != null){
   	   	boolGroup.style.display = "none";	
   	}
	var valeur = document.getElementById("hiddenValeur");
	if(valeur != null){
		//---Create
		var typeConfig = document.querySelector("input[type=radio][name=type]:checked");
		if(typeConfig != null){
			displayFormconfig(typeConfig.value,12, valeur.value);
		}
		
		var radioType = document.querySelectorAll('input[type=radio][name=type]');
	   	Array.from(radioType).forEach(function(link) {
	   		return link.addEventListener('click', function(event) {
				displayFormconfig(this.value, 12,  valeur.value);
		    });
		});
		//--update
		var typeConfigSelect = document.querySelector('select[name=type]');
		if(typeConfigSelect != null){
			displayFormconfig(typeConfigSelect.value ,3, valeur.value);
			var selectType = document.querySelectorAll('select[name=type]');
		   	Array.from(selectType).forEach(function(link) {
		   		return link.addEventListener('change', function(event) {
					displayFormconfig(this.value, 3,  valeur.value);
			    });
			});
		}
	}
	// impression des cartes - popup
    var inprintForm = document.getElementById("IN_PRINTForm");
    if(inprintForm != null){
   	 inprintForm.addEventListener('submit', function(e) {
   	    	window.open('', 'formprint', 'width=800,height=600,resizeable,scrollbars,menubar');
   	   	    this.target = 'formprint';
  		}); 
    }
    
    //Messages modal
    var dialogMsg = document.querySelector('#messageModal #dialog');
    if(dialogMsg != null){
    	var messageModal = document.getElementById('messageModal');
    	var myModalInstance = new Modal(messageModal);
    	myModalInstance.show();
    }
    
    /* SEARCH LONG POLL */	
	var searchLongPoll = {
			debug : false,
			run : false,
			timer : undefined,
			lastAuthDate : 0,
			list : undefined
	};
	searchLongPoll.start = function() {
		if (!this.run) {
			this.run = true;
			this.timer = this.poll();
		}
	}
	searchLongPoll.clear = function() {
	/*var lastleoauth = document.getElementById("lastleoauth");
	  if(lastleoauth != null){
		lastleoauth.innerHTML = "";
	  }*/
	}
	searchLongPoll.stop = function() {
		if (this.run && this.timer != null) {
			clearTimeout(this.timer);
		}
		run = false;		
	}

	searchLongPoll.load = function() {
		if (typeof sgcRootUrl != "undefined" && this.run) {
			var request = new XMLHttpRequest();
			request.open('GET', sgcRootUrl + "manager/searchPoll", true);
			request.onload = function() {
			  if (request.status >= 200 && request.status < 400) {
				  var message = this.response;
					if (message && message.length) {
						if(message != "stop") {
							window.location.href = sgcRootUrl + message;
						}
					}else {
						setTimeout(function(){
							searchLongPoll.timer = searchLongPoll.poll();
						}, 2000);
					}		    
			  }
			};
			request.onerror = function(){  
				 // Plus de session (et redirection CAS) ou erreur autre ... on stoppe pour ne pas boucler
				console.log("searchLongPoll stoppé ");
				};
			request.send();
		}
	}
	searchLongPoll.poll = function() {
		if (this.timer != null) {
			clearTimeout(this.timer);
		}
		//return $(this).delay(1000).load(); 
		setTimeout(searchLongPoll.load(), 1000);    
	}
	if(typeof sgcRootUrl != "undefined") {
		searchLongPoll.start();
	}
	/* SEARCH LONG POLL - END*/
	
	//Popover image liste carte
	var cardListPopover = document.querySelectorAll('#cardList .photo img');
	if(cardListPopover != null){
		for (var i = 0; i < cardListPopover.length; i++){
			new Popover(cardListPopover[i], {
				  trigger: 'hover',
				  placement: 'top',
				  template: '<div class="popover" role="tooltip"><div class="popover-content"><img src="'+ cardListPopover[i].src + '" height="188" width="150"/></div></div>'
				});
		}
	}
	
	//Footer
	var footer =  document.querySelector('#footer span');
	if(footer != null){
		footer.innerHTML = " - " + (new Date()).getFullYear();
	}

	//Workaround pour le 1er bouton supprimer d'un tableau Rooo...
	var deleteClass = document.querySelectorAll('.deleteTableBtn');
   	Array.from(deleteClass).forEach(function(link) {
   		if(link.children[0].id!="command"){
   			var action = link.querySelector('#urlPath').value;
   			var form = document.createElement('form');
   			form.setAttribute("id", "command");
   			form.setAttribute("action", action);
   			form.setAttribute("method", "post");
   			form.innerHTML = link.innerHTML;
   			link.innerHTML = "";
   			link.appendChild(form);
   		}
	});
   	var megamenu = document.querySelectorAll('.megamenu');
   	Array.from(megamenu).forEach(function(link) {
   		return link.addEventListener('change', function(event) {
   		 var searchEppnForm = document.getElementById("searchEppnForm");
   			searchEppnForm.submit();
   	    }); 
   	})
   	
})



