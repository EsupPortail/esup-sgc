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
//Freefield select
function displayResultFreefield(selectedField, fieldsValue, indice) {
	$.ajax({
        url: freeUrl,
        type: 'GET',
        data: {field: selectedField},
        success : function(data) {
			$("#freeFieldValue" + indice).empty();
			if(data.length>1){
				$("#freeFieldValue" + indice).prepend("<option value=''>-- Champ libre : résultats -</option>");
			}
        	$.each(data, function(index, value) {
        		var selected = "";
        		var splitFieldsFirstLevel = fieldsValue.split('@@');
        		
        		if(typeof splitFieldsFirstLevel[indice] != 'undefined'){
	            	if(fieldsValue.length>0 ){
	            		var splitFields2ndLevel = splitFieldsFirstLevel[indice].split(",");
		        		if(splitFields2ndLevel.indexOf(value)!=-1){
		        			selected ="selected='selected'";
		        		}
	            	}
            	}
        		$("#freeFieldValue" + indice).append("<option value='"+ value + "' " + selected +  ">" + value + "</option>");
        	});
        	$('.selectpicker').selectpicker('refresh');
        }
	});
}

//Webcam
function set_webcam() {
	    Webcam.set({
	  	  width: 320,
	  	  height: 240,
	  	  image_format: 'jpeg',
	  	  jpeg_quality: 90,
	  	  upload_name: "webcam"
	  	 });
	  	 Webcam.attach( 'my_camera' );
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
	var orientation = "1";
	
	imageCropper.cropit({
		onImageError : function(e) {
    	if(e.code==1){
    		alert(messages['photoTooSmall']);
    		return false;
    	}
	  },onFileChange: function(e) {
          var file = e.target.files[0]
          if (file && file.name) {
              EXIF.getData(file, function() {
                  var exifData = EXIF.pretty(this);
                  orientation = EXIF.getTag(file, "Orientation");
              });
          }
      }
	});

	imageCropper.cropit('previewSize', { width: 150, height: 188});
	$("#btnRetouche").on("click",function() {
		$("#filterSliders").hide();
		imageCropper.cropit('imageSrc', photorUrl + $("#retouche #cardId").val());
	});
	if(typeof lastId != "undefined" && lastId != "-1" && isRejected != "true" && !fromLdap){
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
    	 imageCropper.find('.cropit-preview-image').attr("id","image-preview");
    	 var imageExif = document.getElementById('image-preview');
    	 //hack iphone,iPad
    	 if(isISmartPhone == "true" && orientation == "6"){
    		 imageCropper.cropit('rotateCW');
    	 }
    	 imageCropper.cropit('exportZoom', 2);
    	 var image = imageCropper.cropit('export', { type: 'image/jpeg', originalSize: false,  quality: .9 });
    	
    	 $('#specimenCarte img#photo').attr("src", image);
    	 imageData.val(image);
    	 //hack iphone,iPad
    	 if(isISmartPhone == "true" && orientation == "6"){
    		imageCropper.cropit('rotateCCW');
    	 }
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
 	
 	 // impression des cartes - popup
    $('#IN_PRINTForm').submit(function() {
    	window.open('', 'formprint', 'width=800,height=600,resizeable,scrollbars,menubar');
   	    this.target = 'formprint';
  	});  

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
	
	$("#downloadOk").on('click', function (e) {
		$('#modalFields').modal('hide')
		$("#searchCsvForm").submit();
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
	
	/* CROUS SmartCard API View*/
	$( ".getCrousSmartCard" ).click(function(e) {
		e.preventDefault(); 
		$.ajax({
			url : this.href,
			context: this,
			success : function(message) {
				if (message && message.length) {
					$(this).parent().next(".getCrousSmartCardDiv").replaceWith(message);
					$(this).parent().remove();
				}
			},
			cache : true
		})
	});

	
    $('#templateCardsList .photo img').popover({ trigger: 'hover', placement : 'auto', content: function () {
		return '<img class="popTemplate" src="'+ $(this)[0].src + '"/>';
		}, html:true});
    
    $('#_cssStyle_id').keyup(function () {
    	$("#mainStyle").remove();
    	$('#specimenCarte').before('<style id="mainStyle">'+ $(this).val() + '</style>');
    });
    $('#_cssMobileStyle_id').keyup(function () {
    	$("#mobileStyle").remove();
    	$('#specimenCarte').before('<style id="mobileStyle">@media screen and (max-width: 450px) {'+ $(this).val() + '}</style>');
    });

    $("#updateTemplateCard").on("click", "#proceed", function() {
        if (confirm('Enregistrer les modifications?')) {
           return true;
        }else{
        	return false
        }
    });
    
    //Si webcam détectée, la partie webcam est affichée
	Webcam.on( 'error', function(err) {
		$("#webCam").remove();
	} );
    var camera = $("#my_camera").val();
    if(typeof camera != "undefined"){
		Webcam.on( 'live', function() {
			// camera is live, showing preview image
			// (and user has allowed access)
			$("#webCam").show();
		} );
	    set_webcam();
	    $("#retryWebcam").on("click", function() {
	    	set_webcam();
	    	$(".cropit-preview-image").removeAttr('src');
	    	imageCropper.cropit('imageSrc', null);
	    });
	    
	    $("#snapShot").on("click", function() {
	   	 Webcam.snap( function(data_uri) {
	   		imageCropper.cropit('imageSrc', data_uri);
	   		imageCropper.cropit('minZoom', 'fill');
	   	 } );
	    })
    }
    
    //Récupération des résultats du select champ libre
    if(typeof freeUrl != "undefined"){
    	var selectedField = $(".freeSelect");
    	 $.each(selectedField, function(index, value) {
    		 var indice = $(this)[0].id.replace("fields","");
    		 displayResultFreefield($(this).val(), fieldsValue, indice);
    	 });
    	
    	$(".freeSelect").change(function(){
    		var indice = $(this)[0].id.replace("fields","");
    		selectedField = $(this).val();
    		displayResultFreefield(selectedField, fieldsValue, indice);
		});
    }
});
