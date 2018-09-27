(function(f) {
    window.equalDiv = f;
})(function(config) {

    'use strict';

    var
        i = 0,
        j = 0,

        minHeight = config.minHeight || 0,
        watchClass = config.watch || 'equalized',
        matchedElems,
        matchedElemHeight = 0,
        matchedElemsFragment,
        shadowContentEl,
        shadowContainers,
            
        numberOfMatchedElems = 0,
        heightArray = [],

        scheduledResizeFrame = false,
        imwContentEl = document.getElementById(config.containerID) || document.getElementById('content'),
     
        matchedElemsContainers = imwContentEl.getElementsByClassName(watchClass),
        matchedElemsContainersLenght = matchedElemsContainers.length,

        currentWidth = window.innerWidth,
        lastKnownWidth = currentWidth;

    function init() {

        scheduledResizeFrame = true;

        lastKnownWidth = currentWidth;

        shadowContentEl = imwContentEl.cloneNode(true);
        shadowContainers = shadowContentEl.getElementsByClassName(watchClass);

        imwContentEl.classList.add('resetEqualizer');

        i=0;
        for (; i < matchedElemsContainersLenght; i++) {

            matchedElems = matchedElemsContainers[i].getElementsByClassName(watchClass + '-watch'); // live elements
            matchedElemsFragment = shadowContainers[i].getElementsByClassName(watchClass + '-watch'); // shadow elements
            numberOfMatchedElems = matchedElems.length;
            
            heightArray = [];

            j=0;
            for (; j < numberOfMatchedElems; j++) {
                matchedElemHeight = matchedElems[j].offsetHeight;
                heightArray.push( matchedElemHeight > minHeight ? matchedElemHeight : minHeight ); // read heights and store them in heightArray
            }
                
            j--;
            for (; j > -1; j--) matchedElemsFragment[j].style.height = Math.max.apply(null, heightArray) + 'px'; // set new height to shadow elements
            
        }		

        requestAnimationFrame(reflow);
        
    }

    function reflow() {

        imwContentEl.parentNode.replaceChild( shadowContentEl, imwContentEl );

        imwContentEl = document.getElementById('content');
        matchedElemsContainers = imwContentEl.getElementsByClassName(watchClass);

        shadowContentEl = null;
        shadowContainers = null;

        scheduledResizeFrame = false;
        
    }


    function onResizeHandler() {

        currentWidth = window.innerWidth; 

        if ( currentWidth === lastKnownWidth || scheduledResizeFrame ) return; 

        init();
        
    }
    
    window.addEventListener('resize', onResizeHandler, false );
    init();

});