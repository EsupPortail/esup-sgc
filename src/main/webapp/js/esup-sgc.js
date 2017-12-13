//Turn alert js in bootstrap style
window.alert = function(message, title) {
    if($("#bootstrap-alert-box-modal").length == 0) {
        $("body").append('<div id="bootstrap-alert-box-modal" class="modal fade">\
            <div class="modal-dialog">\
                <div class="modal-content">\
                    <div class="modal-header" style="min-height:40px;">\
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>\
                        <h4 class="modal-title"></h4>\
                    </div>\
                    <div class="modal-body"><div class="alert alert-warning alert-dismissible" role="alert"><strong></strong></div></div>\
                </div>\
            </div>\
        </div>');
    }
    $("#bootstrap-alert-box-modal .modal-header h4").text(title || "");
    $("#bootstrap-alert-box-modal .modal-body strong").html(message || "");
    $("#bootstrap-alert-box-modal").modal('show');
};

function multiUpdateForm(idArray) {
	if(typeof multiUpdateFormUrl != "undefined"){	
		 $.ajax({
		        url: multiUpdateFormUrl,
		        type: 'POST',
		        data: {cardIds:idArray.toString()},
		        success : function(traitementLotHtml, statut) {
		        	$("#traitementLot").html(traitementLotHtml);
		        }
		 });
	}
};
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
    lion.filters([Konva.Filters.Brighten, Konva.Filters.Enhance]);
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

//Stats bar chart
function multiChartStackBar(allData, id, start, transTooltip,formatDate){
	if(typeof $(id).get(0) != "undefined"){
    	var barLabels = [];
    	var length = Object.keys(allData).length;
    	var dataSets = [];
    	var j=0;
    	$.each(allData, function(idx, obj){ 
    		//Hack : on enlève le '_'
    		lab = idx.substr(1, idx.length);
    		if(formatDate){
    			lab = formatDateString(idx.substr(1, idx.length));
    		}
    		barLabels.push(lab);
    		var k=start;
	    	$.each(obj, function(index, value) {
	        	var values = [];
	        	for (var i = 0; i < length; i++)
	        	{
	        		values.push(0);
	        	}
	    		values[j] = value;
	        	dataSets.push({
	        		label: index,
	        		data: values,
	        		backgroundColor: generateStackColors[k]
	        	});
	        	k++;
	    	});
	    	j++;
    	 });
        var  barChartData = {
            	labels : barLabels,
            	datasets : dataSets
        }
     	var ctx = $(id).get(0).getContext("2d");
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
    if(typeof $(id).get(0) != "undefined"){
    	var legend = true;
		if(option=="legend"){
		 legend = false;
		}
    	var doughnutLabels = [];
    	var values = [];
    	var dataSets = [];
    	var doughnutDataArray =[];
    	$.each(data, function(index, value) {
    		doughnutLabels.push(index);
    		values.push(value);
        });
    	dataSets.push({
    		data: values,
    		backgroundColor:  generateStackColors,
    		hoverBackgroundColor: generateColors
    	});
    	var doughnutDataArray={
			   labels: doughnutLabels,
			   datasets: dataSets
        	};	 	        	
     	var ctx3 = $(id).get(0).getContext("2d");
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
    					var tooltipEl = $(id).next("div")[0];
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

function chartBar(data, id, transTooltip, formatDate){
	if(typeof $(id).get(0) != "undefined"){
    	var listLabels = [];
    	var listValeurs = [];
    	var listTooltipLabels = [];
    	$.each(data, function(index, value) {
    		if(formatDate){
    			index = formatDateString(index);
    		}
    		listTooltipLabels.push(index);
    		if (index.length > 12) {
    			index = index.substring(0, 12) +".";
    		}
        	if(transTooltip != null){
        		b = index.replace(/_/g,"");
        		msg = transTooltip + b.charAt(0).toUpperCase() + b.slice(1).toLowerCase(); 
        		index = messages[msg];
        	}
    		listLabels.push(index);
    		listValeurs.push(value);
        });
        var  barChartData = {
            	labels : listLabels,
            	datasets : [
            		{
    		            backgroundColor: generateColors[3],
    		            borderColor: generateBorderColors[3],
    		            borderWidth: 1,
            			data : listValeurs
            		}
            	]
            }
     	var ctx = $(id).get(0).getContext("2d");
    	myBar = new Chart(ctx, {
    		type: 'bar',
    		data: barChartData,
    		options: {responsive : true,
    			legend: {
    				display: false
    			},
    			tooltips: {
    				bodyFontSize :  30,
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
 	if(typeof $(id).get(0) != "undefined"){
    	var inlineValeurs = [];
    	var inlineDatasets = [];
    	var a=0;
    	//dates
    	var xValues = [];
    	var dates = arrayDates;
    	//Hack : on enlève le '_'
		$.each(data, function(index, value) {
			if(!byMonth){			
				xValues= Object.keys(value).filter(function (propertyName) {
				    return propertyName.substr(1, propertyName.length);
				});
				$.each(dates, function(ind, val) {
	    			if($.inArray(val, xValues)>-1){
		        		inlineValeurs.push(value[val]);
	    			}else{
		        		inlineValeurs.push(null);
	    			}	
				});
			}else{
				var keyMois = Object.keys(value);
				for(i=1;i<=12;i++){
	    			if($.inArray(i.toString(), keyMois)>-1){
		        		inlineValeurs.push(value[i]);
	    			}else{
		        		inlineValeurs.push(null);
	    			}			
				}
			}
            inlineDatasets.push({
            	//Hack : on enlève le '_'
        	     label: index.substr(1, index.length),
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
        });
		var dateLabels = dates;
		if(formatDate){
			dateLabels = [];
			$.each(dates, function(ind, val) {
				dateLabels.push(formatDateString(val));
			});
		}
     	var dataMois = {
 		    labels: dateLabels,
 		    datasets: inlineDatasets
 		};      
     	var ctx3 = $(id).get(0).getContext("2d");
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
	
    switch(val) {
	    case 'HTML':
	   	 	$("#boolGroup").remove();
	   	 	$("#valeur").remove();
	   	 	$("#areaEditor").after("<div class='col-md-" + col +"'><textarea class='form-control simple-editor' id='valeur' name='value'>" + valeur + "</textarea></div>");
	   		$.trumbowyg.svgPath = '/css/ui/icons.svg';
	   		$('.simple-editor').trumbowyg({
	   			btns: [
	   		        ['viewHTML'],
	   		        ['formatting'],
	   		        ['bold', 'italic', 'underline', 'strikethrough'],
	   		        ['link'],
	   		        'btnGrp-justify',
	   		        'btnGrp-lists',
	   		        ['removeformat'],
	   		        ['fullscreen'],
	   		        ['foreColor', 'backColor']
	   		    ]
	   		});
	        break;
	    case 'TEXT':
		   	 $("#boolGroup").remove();
		   	 $('.trumbowyg-box ').remove();
		   	 $("#valeur").remove();
		   	 $("#areaEditor").after("<div class='col-md-" + col +"'><textarea class='form-control simple-editor' id='valeur' name='value'>" + valeur + "</textarea></div>");
	        break;
	    case 'BOOLEAN':
		   	 $('.trumbowyg-box ').remove();
		   	 $('#valeur').remove();
		   	 $("#areaEditor").after(bool);
	        break;
    }
}

$(document).ready(function() {
	if($('#cardList .table tr').length<200) {
		$('#cardList .table').footable();
	}
	$('[data-toggle="popover"]').popover();   
	//CROPIT
	var imageCropper = $('#image-cropper');
	var cropitPreview = $('#cropit-preview');
    var exportAdminBtn = $('#image-cropper .exportAdmin');
    var previewBtn = $('#image-cropper .preview');
    var imageData = $("#image-cropper .cropit-image-data");
	var imageInput = $('#image-cropper .cropit-image-input');
	var confirmPreview = $('#confirmPreview');
	var alertPreview = $('#alertPreview');
	var filterSliders = $("#filterSliders");
	var container = $("#container");
	var cropitInfo = $(".cropit-info");
	
	imageCropper.cropit({onImageError : function(e) {
    	if(e.code==1){
    		alert(messages['photoTooSmall']);
    		return false;
    	}
	  }
	});
	
	imageCropper.cropit('previewSize', { width: 150, height: 188});
	$("#btnRetouche").on("click",function() {
		$("#filterSliders").hide();
		imageCropper.cropit('imageSrc', photorUrl + $("#retouche #cardId").val());
	});
	if(typeof lastId != "undefined" && lastId != "-1" && isRejected != "true"){
		imageCropper.cropit('imageSrc', lastPhotoUrl);
	}
	imageCropper.cropit('maxZoom', 1.8);
	imageCropper.cropit('minZoom', 'fit');
	cropitPreview.on('dragover', function(e) {
		cropitPreview.css({"border" : "2px dashed black"});
	});
	cropitPreview.on('dragleave', function(e) {
		cropitPreview.removeAttr("style");
		cropitPreview.css("border", "2px dashed #ccc");
	});
	
    $(".rotate-ccw").on("click",function() {
    	imageCropper.cropit('rotateCCW');
    	imageCropper.cropit('exportZoom', 1);
    	confirmPreview.val("false");
    	alertPreview.removeClass("alert-success" ).addClass( "alert-danger" );
    	alertPreview.html(" <span class='glyphicon glyphicon-warning-sign'><!----></span> " + messages['alertPreview']);	
    });
    $(".rotate-cw").on("click",function() {
    	imageCropper.cropit('rotateCW');
    	imageCropper.cropit('exportZoom', 1);
    	confirmPreview.val("false");
    	alertPreview.removeClass("alert-success" ).addClass( "alert-danger" );
    	alertPreview.html(" <span class='glyphicon glyphicon-warning-sign'><!----></span> " + messages['alertPreview']);
    });
    
    imageInput.change(function(e){
    	imageCropper.cropit('exportZoom', 1);
    	confirmPreview.val("false");
    	alertPreview.removeClass("alert-success" ).addClass( "alert-danger" );
    	alertPreview.html(" <span class='glyphicon glyphicon-warning-sign'><!----></span> " + messages['alertPreview']);
	});
    
	$("#cardRequest [type=range]").change(function(){
    	imageCropper.cropit('exportZoom', 1);
    	confirmPreview.val("false");
    	alertPreview.removeClass("alert-success" ).addClass( "alert-danger" );
    	alertPreview.html(" <span class='glyphicon glyphicon-warning-sign'><!----></span> " + messages['alertPreview']);
	});

    previewBtn.on("click",function() {
    	var currentImg = $(".cropit-preview-image").prop("src");
    	if(currentImg == ""){
          	 alert(messages['fileRequired']);      	 
          	 return false;
           }
    	imageCropper.cropit('exportZoom', 2);
    	var image = imageCropper.cropit('export', { type: 'image/jpeg', originalSize: false,  quality: .9 });  
    	$('#specimenleocarte img#photo').attr("src", image);
    	imageData.val(image); 
    });
    
    exportAdminBtn.on("click",function() {
    	if(typeof $("#container canvas").val() == "undefined"){
	    	var currentImg = $(".cropit-preview-image").prop("src");
	    	if(currentImg == ""){
	          	 alert(messages['fileRequired']);      	 
	          	 return false;
	           }
	    	imageCropper.cropit('exportZoom', 2);
	       	var image = imageCropper.cropit('export', { type: 'image/jpeg', originalSize: false,  quality: .9 });  
	       	imageData.val(image);
    	}
       	else{
       		canvas = document.getElementById("container").querySelector("canvas");
       		var data64 = canvas.toDataURL('image/jpeg', 0.9);
       		imageData.val(data64);
       	}
     });
    
    //CANVAS
    var img1 = new Image();
    $("#btnColors").on("click",function() {
    	cropitPreview.hide();
    	filterSliders.show();
    	cropitInfo.hide();
    	container.show();   
    	cropitPreview.hide();
    	var currentImg = $(".cropit-preview-image").prop("src");
    	if(currentImg == ""){
          	 alert(messages['fileRequired']);      	 
          	 return false;
           }
    	imageCropper.cropit('exportZoom', 2);
       	var image = imageCropper.cropit('export', { type: 'image/jpeg', originalSize: false,  quality: .9 });  
        var sources = {
      	      lion: image
      	    };
      	loadImages(sources, buildStage);
      	$('input[type=range]').val(0);
    });
    
    $("#resetBtn").on('click', function(e) {
    	imageCropper.cropit('exportZoom', 2);
    	imageCropper.cropit('imageSrc', photorUrl + $("#retouche #cardId").val());
       	filterSliders.hide();
       	cropitInfo.show();
    	cropitPreview.show();
    	container.empty(); 
        $('input[type=range]').val(0);
      });

    $("#scaleBtn").on('click', function(e) {
    	canvas = document.getElementById("container").querySelector("canvas");
    	if(canvas !=null){
	   		var data64 = canvas.toDataURL('image/jpeg', 0.9);
	   		imageCropper.cropit('imageSrc', data64);
	   		filterSliders.hide();
	   		cropitInfo.show();
	    	cropitPreview.show();
	    	container.empty();
    	}
      });
    
    //Validation formulaire demande de carte 
	var radio1 = $("#radio1");
	var radio2 = $("#radio2");
	var radioCnil1= $("#radioCnil1");
	var radioCnil2 = $("#radioCnil2");
	var radioEurope1= $("#radioEurope1");
	var radioEurope2 = $("#radioEurope2");
	var reglement = $("#reglement");
    $("#cardRequest").on("submit",function() {
    	 if(imageData == "" ){
    		alert (messages['alertPhoto']);
    		return false;
    	}
      	if(confirmPreview.val() == "false"){
    		alert (messages['alertPreview']);
    		return false;
    	}    
     	if((!radio1.is(":checked"))&&(!radio2.is(":checked"))&&radio1.length>0){
    		alert (messages['alertCrous']);
    		return false;
    	}
     	if((!radioEurope1.is(":checked"))&&(!radioEurope2.is(":checked"))&&radioEurope1.length>0){
    		alert (messages['alertEurope']);
    		return false;
    	}      	
     	if((!radioCnil1.is(":checked"))&&(!radioCnil2.is(":checked"))&&radioCnil1.length>0){
    		alert (messages['alertCnil']);
    		return false;
    	}   
     	if((!reglement.is(":checked"))&&reglement.length>0){
    		alert (messages['alertRules']);
    		return false;
    	}    
     	var adresseLength =  $("#adresseInterne").val().trim().length;
     	var radio3 = $("#radio3");
     	if((radio3.is(":checked"))&&(adresseLength>250)){
    		alert (messages['alertTooChar']);
    		return false;	
     	}
    });
	
   	$("#confirmPhoto").on("click",function(event){
   		confirmPreview.val("true");
   		alertPreview.removeClass("alert-danger" ).addClass( "alert-success" );
   		alertPreview.html(" <span class='glyphicon glyphicon-ok'><!----></span> " + messages['confirmPreview']);
   		$('#previewCarte').modal('hide');
   	});
   	
   	//Traitemnt par lot  cartes
   	var idArray = [];
   	var currentEtat = "";
	$("#selectall").click(function () {
		idArray = [];
		$('.case').prop('checked', this.checked);
		$.each($("input[name='case']:checked"), function(index, value){            
			idArray.push($(this).val());
	    });
		multiUpdateForm(idArray);
		$("#listeIds").val(idArray);
	});
	 
	$(".case").click(function(){
		if($.inArray( $(this).val(), idArray )< 0){
			idArray.push($(this).val());
		}else{
			idArray.splice(idArray.indexOf($(this).val()), 1 );
		}

		if($(".case").length == $(".case:checked").length) {
			$("#selectall").attr("checked", "checked");
		} else {
			$("#selectall").removeAttr("checked");
		}
		multiUpdateForm(idArray);
		$("#listeIds").val(idArray);
		if(idArray.length == 0){
			$("#etatsChoice").empty();
		}
	});
	
	$(window).on('load', function(){ 
		$("#selectall").removeAttr("checked");
		$(".case").removeAttr("checked");
	});

    //Cartes : edition des messages

	$(document).on("click","#msgChoice button[id^='msg']", function() {
		var realid = this.id;
		var splitRealid = realid.split("_");
		var realSpan = "#span_" + splitRealid[1];
		var comment = $(realSpan).html().replace("&nbsp;","");
		$("#comment").val(comment);
	});
   	
     $('#cardList .photo img').popover({ trigger: 'hover', placement : 'auto', content: function () {
	    								return '<img src="'+ $(this)[0].src + '" height="188" width="150"/>';
 									}, html:true});
    
   	// SGC Encode
 	if (typeof sgcRootUrl != "undefined") {
		$.ajax({
			url : sgcRootUrl + "manager/nfc/current",
			context: this,
			success : function(message) {
				if (message && message.length) {
					alertMsg = messages['alertEncoding'] + message + ".";
					alertMsg += "<br/><a href=\"" + sgcRootUrl + "manager/nfc/clear\">" + messages['cancel'] + "</a>";
					alert(alertMsg, messages['encodingInprogress']);
				}
			},
			cache : false
		})
	}
 	
 	//autocomplete eppn
 	var searchBox = document.querySelector('#searchEppn');
 	if(searchBox != null){
	 	var searchEppn = $("#searchEppn");
	 	var awesomplete = new Awesomplete(searchBox, {
	 		  minChars: 3,
	 		  maxItems: 10
	 		});
		searchEppn.on("keyup", function(){
			if(this.value.length>2) {
	 		  $.ajax({
	 		    url: searchEppnUrl,
	 		    data : {searchString: this.value},
	 		    type: 'GET',
	 		    dataType: 'json'
	 		  })
	 		  .success(function(data) {
	 		    var list = [];
	 		    $.each(data, function(key, value) {
	 		      var labelValue =value.eppn;
		 		  if($.inArray(labelValue, this._list)<0){
		 			list.push(labelValue);
		 		  }
	 		    });
	 		    awesomplete.list = list;
	 		  });
			}
		});
		searchEppn.on('awesomplete-selectcomplete',function(){
	 		$("#searchEppnForm").submit();
	 	});
	}
	// impression des cartes - popup
    $('#IN_PRINTForm').submit(function() {
    	window.open('', 'formprint', 'width=800,height=600,resizeable,scrollbars,menubar');
   	    this.target = 'formprint';
  	});  

    //Formulaire paybox
    if(typeof displayPayboxForm != "undefined"){
	    if(displayPayboxForm){
	    	$("#payboxForm").submit();
	    }
    }
    
    //Onglets Cartes
    $("#cardList .nav-tabs li a").on('click', function () {
    	var type = $(this.attributes.href)[0].value;
    	type = type.substr(1,3);
    	$("#searchEppnForm #type").val(type);
		$("#address").val("");
		$("#etat").val("");
		$("#editable").val("all");
		$("#ownOrFreeCard").val("false");
		$("#searchEppn").val("");
		$("#nbCards").val("");
		$("#nbRejets").val("");
		$("#flagAdresse").val("");
    	$("#searchEppnForm").submit();
    }); 
    
    //Tabs Stats Leo
    
    $("#statsManager .nav-tabs li a").on('click', function () {
    	var type = $(this.attributes.href)[0].value;
    	type = type.substr(1,3);
    	if(type == 0){
        	if(typeof statsRootUrl != "undefined"){
        		window.location.href = 	statsRootUrl;
        	};	
    	}else{
	    	if(typeof statsUrl != "undefined"){
	    		window.location.href = 	tabsUrl + type;
	    	};
    	}
    });  
    
    //stats
	if(typeof statsUrl != "undefined"){
	    $.ajax({
	        url: statsUrl,
	        type: 'GET',
	        dataType : 'json',
	        data: {typeInd: selectedType},
	        success : function(data) {
	        	multiChartStackBar(data.cardsByYearEtat, "#cardsByYearEtat",3, "etat");
	        	chartPieorDoughnut(data.crous, "#crous", "pie", null);
	        	chartPieorDoughnut(data.difPhoto, "#difPhoto", "pie", null);
				chartBar(data.cardsByDay, "#cardsByDay", null, true);
				multiChartStackBar(data.paybox, "#paybox",3);
				chartBar(data.motifs, "#motifs", "motif");
				lineChart(data.dates, "#dates", true, monthsArray, true);
				chartBar(data.deliveredCardsByDay, "#deliveredCardsByDay", null, true);
				chartBar(data.encodedCardsByday, "#encodedCardsByday", null, true);
				chartPieorDoughnut(data.nbCards, "#nbCards", "doughnut", null);
				chartBar(data.editable, "#editable");
				chartPieorDoughnut(data.verso5, "#verso5", "doughnut", null);
				chartPieorDoughnut(data.browsers, "#browsers", "pie", null);
				chartPieorDoughnut(data.os, "#os", "pie", null);
				chartPieorDoughnut(data.nbRejets, "#nbRejets", "doughnut", null);
				chartBar(data.notDelivered, "#notDelivered");
				multiChartStackBar(data.cardsMajByDay, "#cardsMajByDay",3, null,true);
				chartPieorDoughnut(data.cardsMajByIp, "#cardsMajByIp", "doughnut", null);
				lineChart(data.cardsMajByDay2, "#lineCardsMajByDay", false, oneMonthBefore, false, true);
				chartPieorDoughnut(data.deliveryByAdress, "#deliveryByAdress", "pie", "legend");
				chartBar(data.userDeliveries, "#userDeliveries", null, true);
	        }
	    });    
	}
	$("select#etat").change(function(){
		if(typeof filterAdressUrl != "undefined"){	
			var selectedEtat = $("#etat").val();
            var selectedTab = $("#tabTypes .active a")[0].hash;
            var tabActif = selectedTab.substr(1,3);
			
			 $.ajax({
			        url: filterAdressUrl,
			        type: 'GET',
			        data: {etat: selectedEtat, tabType : tabActif},
			        success : function(data) {
						$("select#address").empty()
						if(data.length>1){
							$("select#address").prepend("<option value=''>Tous</option>");
						}
			        	$.each(data, function(index, value) {
			        		$("select#address").append("<option value='"+ value + "'>" + value + "</option>");
			        	});
			        }
			 });
		}
	});
	$("#nofilter").on('click', function () {
		$("#address").val("");
		$("#etat").val("");
		$("#editable").val("all");
		$("#ownOrFreeCard").val("false");
		$("#searchEppn").val("");
		$("#nbCards").val("");
		$("#nbRejets").val("");
		$("#flagAdresse").val("");
		$("#searchEppnForm").submit();
	});

	
	$("#sessionsdiv").insertBefore("#sessionsUsers");
	
	$("#downloadOk").on('click', function (e) {
		$('#modalFields').modal('hide')
		$("#searchCsvForm").submit();
	});
	
	$("#selectAllFields").click(function () {
		$('.caseField').prop('checked', this.checked);
	});
	 
	$(".caseField").click(function(){
		if($(".caseField").length == $(".caseField:checked").length) {
			$("#selectAllFields").attr("checked", "checked");
		} else {
			$("#selectAllFields").removeAttr("checked");
		}
	});
	
	//Configs
	$("#boolGroup").hide();
	var valeur = $("#hiddenValeur").val();
	//---Create
	var typeConfig = $('input[type=radio][name=type]:checked').val();
	displayFormconfig(typeConfig,12,valeur);
	$('input[type=radio][name=type]').on('change', function() {
		displayFormconfig($(this).val(),12,valeur)
	});
	//--update
	var typeConfigSelect = $('select[name=type]').val();
	displayFormconfig(typeConfigSelect,3,valeur);
	$('select[name=type]').on('change', function() {
		displayFormconfig($(this).val(),3,valeur)
	});
	
    //Tabs Configs
    $("#configList .nav-tabs li a").on('click', function () {
    	var type = $(this.attributes.href)[0].value;
    	type = type.substr(1,type.length);
    	if(type != 0){
	    	if(typeof tabsUrl != "undefined"){
	    		$("input[name=searchField]").val(type);
	    		$("#tabConfigsForm").submit();
	    	};
    	}else{
    		window.location.href = 	configUrl;
    	}
    }); 
    
    //Mise en page libre stats
    if (typeof statsRootUrl != "undefined") {
	    dragula([document.getElementById('statsPanels')]).on('dragend', dragend);
	    
	    function dragend (el) {
	    	var statsArray =[];
	    	$('.statsDrag').each(function() {
	    			statsArray.push(this.id);
	    	});
	    	setPrefStats(statsArray, "STATS");
	   	}
	
	    function setPrefStats(statsArray, key) {
			$.ajax({
				url : statsRootUrl +"/prefs",
			    data : {values: statsArray.toString(), key: key}
			})
	    }
	    
	    if(prefsStats!=null && prefsStats !=""){
	    	$.each(prefsStatsRm, function(index, value){
	    		$("#statsPanels #" + value).remove();
	    	});
	    	$.each(prefsStats, function(index, value){
		    		var panel = $("#statsPanels #" + value).detach();
				    panel.appendTo("#statsPanels");	    			
	        });
	    }
	    
	    function removeFromArray(value, arr) {
	        return $.grep(arr, function(elem, index) {
	            return elem !== value;
	        });
	    };
	    
	    //Remove stats
	    $(".remove").on('click', function (e) {
	    	prefsStatsRm.push(this.closest(".statsDrag").id);
	    	if(prefsStats==""){
		    	var statsArray =[];
		    	$('.statsDrag').each(function() {
		    		statsArray.push(this.id);
		    	});
		    	setPrefStats(statsArray, "STATS");
	    	}
	    	setPrefStats(prefsStatsRm, "STATSRM");
	    	this.closest(".statsDrag").remove();
	    	//refresh modal
	    	$("#mainModal").load(location.href+" #mainModal>*","");
		});
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
		// $('#lastleoauth').html('');
	}
	searchLongPoll.stop = function() {
		if (this.run && this.timer != null) {
			clearTimeout(this.timer);
		}
		run = false;		
	}
	searchLongPoll.poll = function() {
		if (this.timer != null) {
			clearTimeout(this.timer);
		}
		return $(this).delay(1000).load(); 
		//return setTimeout(this.load, 1000);
	}
	searchLongPoll.load = function() {
		if (typeof sgcRootUrl != "undefined" && this.run) {
			$.ajax({
				url : sgcRootUrl + "manager/searchPoll",
				context: this,
				success : function(message) {
					if (message && message.length) {
						if(message != "stop") {
							window.location.href = sgcRootUrl + message;
						}
					} else {
						setTimeout(function(){
							searchLongPoll.timer = searchLongPoll.poll();
						}, 2000);
					}
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					 // Plus de session (et redirection CAS) ou erreur autre ... on stoppe pour ne pas boucler
					console.log("searchLongPoll stoppé - " + errorThrown);
				},
				cache : false
			})
		}
	}
	$.ajaxSetup({cache:false});
	if(typeof sgcRootUrl != "undefined") {
		searchLongPoll.start();
	}
	/* SEARCH LONG POLL - END*/	
	
	
	/* CROUS RightHolder API View*/
	$( "#getCrousRighHolder" ).click(function() {
	 	if (typeof getCrousRightHolderUrl != "undefined") {
			$.ajax({
				url : getCrousRightHolderUrl,
				context: this,
				success : function(message) {
					if (message && message.length) {
						$("#getCrousRighHolderDiv").append(message);
						$("#getCrousRighHolder").prop("disabled", true);
					}
				},
				cache : true
			})
		}
	});
   $("#typeLogs").on('change', function () {
	   $("#formTypeLogs").submit();
   });
   $("#actionLogs").on('change', function () {
	   $("#formActionLogs").submit();
   });
   $("#retCodeLogs").on('change', function () {
	   $("#formRetCodeLogs").submit();
   });   
});
	
