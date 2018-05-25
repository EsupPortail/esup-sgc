$(document).ready(function() {
	if($('#cardList .table tr').length<200) {
		$('#cardList .table').footable();
	}
	$('[data-toggle="popover"]').popover();   
    
     $('#cardList .photo img').popover({ trigger: 'hover', placement : 'auto', content: function () {
	    								return '<img src="'+ $(this)[0].src + '" height="188" width="150"/>';
 									}, html:true});
 	
	
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
	
});
