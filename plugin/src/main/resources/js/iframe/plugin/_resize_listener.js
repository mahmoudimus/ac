(window.AP || window._AP).define("_resize_listener", ["_dollar"], function ($) {

    "use strict";

    // Normalize overflow/underflow events across browsers
    // http://www.backalleycoder.com/2013/03/14/oft-overlooked-overflow-and-underflow-events/
    function addFlowListener(element, type, fn){
        var flow = type == 'over';
        element.addEventListener('OverflowEvent' in window ? 'overflowchanged' : type + 'flow', function(e){
            if (e.type == (type + 'flow') ||
                ((e.orient == 0 && e.horizontalOverflow == flow) ||
                    (e.orient == 1 && e.verticalOverflow == flow) ||
                    (e.orient == 2 && e.horizontalOverflow == flow && e.verticalOverflow == flow))) {
                e.flow = type;
                return fn.call(this, e);
            }
        }, false);
    };

    // Adds a resize listener to a DOM element (other within <body>). It first adds a set of invisible
    // "sensor" divs to the bottom of the selected element. Those sensor divs serve as over/underflow
    // detectors using the addFlowLister. The flowListener triggers the over/underflow within the div
    // which tells us that the element has resized. We compare the previous and current size. If it's
    // changed, we trigger the resize event.
    //
    // This listener is initiated during the page load event in _init.js. The callback function is
    // the actual iframe resize function in env.js.
    function addListener(element, fn){
        var resize = 'onresize' in element;
        if (!resize && !element._resizeSensor) {
            $("head").append({tag: "style", type: "text/css", $text: ".ac-resize-sensor,.ac-resize-sensor>div {position: absolute;top: 0;left: 0;width: 100%;height: 100%;overflow: hidden;z-index: -1;}"});
            var sensor = element._resizeSensor = document.createElement('div');
            sensor.className = 'ac-resize-sensor';
            sensor.innerHTML = '<div class="ac-resize-overflow"><div></div></div><div class="ac-resize-underflow"><div></div></div>';

            var x = 0, y = 0,
            first = sensor.firstElementChild.firstChild,
            last = sensor.lastElementChild.firstChild,
            matchFlow = function(event){
                var change = false,
                width = element.offsetWidth;
                if (x != width) {
                    first.style.width = width - 1 + 'px';
                    last.style.width = width + 1 + 'px';
                    change = true;
                    x = width;
                }
                var height = element.offsetHeight;
                if (y != height) {
                    first.style.height = height - 1 + 'px';
                    last.style.height = height + 1 + 'px';
                    change = true;
                    y = height;
                }
                if (change && event.currentTarget != element) {
                    var event = document.createEvent('Event');
                    event.initEvent('resize', true, true);
                    element.dispatchEvent(event);
                }
            };

            if (getComputedStyle(element).position === 'static'){
                element.style.position = 'relative';
                element._resizeSensor._resetPosition = true;
            }
            addFlowListener(sensor, 'over', matchFlow);
            addFlowListener(sensor, 'under', matchFlow);
            addFlowListener(sensor.firstElementChild, 'over', matchFlow);
            addFlowListener(sensor.lastElementChild, 'under', matchFlow);
            element.appendChild(sensor);
            matchFlow({});
        }
        var events = element._flowEvents || (element._flowEvents = []);
        if ($.inArray(fn, events) == -1) events.push(fn);
        if (!resize) element.addEventListener('resize', fn, false);
        element.onresize = function(e){
            $.each(events,function(idx,fn){
                fn.call(element, e);
            });
        };
    };

    return {
      addListener: addListener
    };

});
