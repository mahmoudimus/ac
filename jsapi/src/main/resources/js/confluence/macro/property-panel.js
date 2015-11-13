(function($, define){

    define("ac/confluence/macro/property-panel", ["connect-host", "ac/dialog", "confluence/position"], function(_AP, dialog, Position) {

        // When openCustomEditor is invoked, it will assign a function for saving the macro
        // being edited to this field. This simplifies the client's job of saving the macro
        // values - they only need to pass back the updated values - and works because only
        // a single macro editor can be open at a time.
        var saveMacro,
            openEditorMacroBody,
            openEditorMacroData;

        /**
         * Add the property panel arrow/notch
         * @param parent - the property panel root element
         * @param shouldFlip - true if the arrow should appear below the property panel
         */
        var addArrow = function(parent, shouldFlip) {
            var $arrow = $('<div class="property-panel-arrow"></div>');
            if (shouldFlip) {
                $arrow.addClass('property-panel-bottom-arrow').css({ top: parent.outerHeight() });
            }
            parent.prepend($arrow);

            return $arrow;
        };


        /**
         * figures out if there is room above relative to the container to draw the propery panel
         * @param container - the container element you wish to test against
         * @param anchor - the element the property panel is attached to
         */
        var shouldDisplayAbove = function(container, anchor, panel, padding) {
            var panelHeight = panel.outerHeight();
            var heightNeeded = panelHeight + ~~padding;
            var spaceAvailable = Position.spaceAboveBelow(container[0], anchor);

            // Prefer below the anchor if possible.
            if(spaceAvailable.below >= heightNeeded) {
                return false;
            }

            // If space above, flip, otherwise don't (snapToElement will display at bottom of container.)
            return (spaceAvailable.above >= heightNeeded);
        };

        /**
         * Calculates the top and left pixels to locate the property-panel correctly with respect to its anchor element.
         * @param propertyPanel the AJS.Confluence.PropertyPanel to relocate w.r.t. its anchor and panel
         * @param options map of options for the repositioning, including:
         *          - delay : wait {delay} milliseconds before calculating and repositioning
         *          - animate : if true, animate the panel from its current position to the calculated one
         */
        var snapToElement = function (propertyPanel, options) {
            options = options || {};
            window.setTimeout( function() {
                var offset =  AJS.Rte.Content.offset(propertyPanel.anchor);
                var ppWidth = propertyPanel.panel.width();
                var overlap = ppWidth + offset.left - $(window).width() + 10;
                var gapForArrowY = 7;
                var gapForArrowX = 0;
                var elemHeight = $(propertyPanel.anchor).outerHeight();
                var top;
                var left = offset.left - (overlap > 0 ? overlap : 0) - gapForArrowX;

                if(propertyPanel.shouldFlip) {
                    top = offset.top - gapForArrowY - propertyPanel.panel.outerHeight() - 4; //acount for shadow
                }
                else {
                    top = offset.top + gapForArrowY + elemHeight;
                }

                if (propertyPanel.options.anchorIframe) {
                    // The anchor is in an iframe, so the Property Panel should display no lower than the bottom of the iframe.
                    var $iframe = $(propertyPanel.options.anchorIframe);
                    var iframeBottom = $iframe.offset().top + $iframe.height() - propertyPanel.panel.outerHeight() - 10;
                    top = Math.min(top, iframeBottom);
                }
                // position the arrow 10 pixels from the left of the anchor
                propertyPanel.panel.find(".property-panel-arrow").css({
                    left: Math.min(Math.abs(offset.left - left) + 16, ppWidth - 12)
                });

                // CONFDEV-1553. Ensure that the property panel is at always on screen, and not outside the display area
                // due to the positioning of the parent.
                left = Math.max(0, left);

                var css = {
                    top: top ,
                    left: left,
                    'z-index': 3000
                };
                var toAnimate = propertyPanel.panel.add();
                //might move this out to an if statement if this code sticks around
                options.animate ? toAnimate.animate(css, options.animateDuration) : (function(){ toAnimate.css(css); })();

            }, options.delay || 0);
        };


        var module = {
            /**
             * Saves the macro currently being edited. Relies on openCustomEditor() first being invoked by MacroBrowser.
             *
             * @param {Object} updatedMacroParameters the updated parameters for the macro being edited.
             * @param {String} updatedMacroBody the (optional) body of the macro
             */
            saveMacro: function(updatedMacroParameters, updatedMacroBody) {
                if (!saveMacro) {
                    $.handleError("Illegal state: no macro currently being edited!");
                }
                saveMacro(updatedMacroParameters, updatedMacroBody);
                saveMacro = undefined;
            },

            /**
             * Closes the macro editor if it is open. If you need to persist macro configuration, call <code>saveMacro</code>
             * before closing the editor.
             */
            close: function() {
                dialog.close();
            },

            /**
             * Returns the macro parameters of the macro being edited in the macro editor
             * @param callback the callback function which will be called with the parameter object
             */
            getMacroData: function(callback){
                return callback(openEditorMacroData);
            },

            /**
             * Returns the macro body of the macro being edited in the macro editor
             * @param callback the callback function which will be called with the macro body
             */
            getMacroBody: function(callback){
                return callback(openEditorMacroBody);
            },

            /**
             * Creates a new PropertyPanel instance with the supplied content and attaches it to the supplied element.
             *
             * @param anchor {Element} the element to anchor the PropertyPanel to
             * @param content {Element} the content to display inside the PropertyPanel
             * @param options {Object} map of options for the panel, e.g.
             *                  anchorIframe - specifies the iframe that the anchor is inside of
             */
            create: function (type, anchor, content, options, macroData, opts) {
                options = options || {};
                AJS.Rte.BookmarkManager.storeBookmark();
                var parent = $("#property-panel");
                var panel;
                // this will default the value to true if not presesent, otherwise undefined would be false
                var enableFlip = options.enableFlip == undefined || options.enableFlip;
                var shouldFlip;
                parent.length && this.destroy();


                parent = AJS("div").addClass("aui-property-panel-parent").addClass(type + "-panel aui-box-shadow").attr("id", "property-panel").appendTo("body");
                panel = AJS("div").addClass("aui-property-panel").append(content);

                openEditorMacroData = macroData.params;
                openEditorMacroBody = macroData.body;

                function getIframeHtmlForMacro(url) {
                    var data = {
                        "width": "100%",
                        "height": "100%",
                        "ui-params": _AP.uiParams.encode({dlg: 1}),
                        "classifier": "property-panel"
                    };
                    $.extend(data, openEditorMacroData);
                    return $.ajax(url, {
                        data: data
                    });
                }

                getIframeHtmlForMacro(opts.url).done(function(data){
                    var panelHtml = $(data);
                    panelHtml.addClass('panel-frame');
                    $(".aui-property-panel").append(panelHtml);
                });


                //as the element needs to have a display block, to calculate the height for rapheal
                //position it top of screen and off stage left so it doesnt flicker.
                parent.append(panel).css({
                    top: 0,
                    left: -10000
                });
                shouldFlip = enableFlip && shouldDisplayAbove($(options.anchorIframe || $(anchor).parent()), $(anchor),parent,10);
                var that = this;
                //remove the margin from the last element, as its applied as padding to the container
                content.find(".last:last").css({"margin-right":0});


                var arrow = addArrow(parent,shouldFlip);

                //TODO: I've forgotten the important things I learnt about 'this'. Need to reread Sergio's slides.
                var PropertyPanel = this;

                this.current = {
                    anchor: anchor,
                    panel: parent,
                    hasAnchorChanged: function (el) {
                        return el && that.hasAnchorChanged(el);
                    },
                    snapToElement : function (options) {
                        snapToElement(this, options);
                    },
                    shouldFlip : shouldFlip,
                    tip: arrow,
                    options: options,
                    updating: true,
                    type: type
                };

                snapToElement(this.current);
                panel = this.current;
                $(document).bind("keydown.property-panel.escape", function(e) {
                    if (e.keyCode === 27) { // esc key
                        PropertyPanel.destroy();
                    }
                });
                $(document).bind("click.property-panel",function (e) {
                    // If click fired inside active property panel - ignore it
                    if (!$(e.target).closest("#property-panel").length) {
                        PropertyPanel.destroy();
                    }
                });

                AJS.trigger("created.property-panel", this.current);
                this.current.updating = false;
                return this.current;
            },

            /**
             * Tears down the current PropertyPanel.
             */
            destroy: function () {
                //if current is bound, then shadow and tip is as well
                if (!this.current) {
                    AJS.log("PropertyPanel.destroy: called with no current PropertyPanel, returning");
                    return;
                }
                if (this.current.updating) {
                    AJS.log("PropertyPanel.destroy: called while updating, returning");
                    return;
                }
                AJS.trigger("destroyed.property-panel", this.current);
                $(document).unbind(".property-panel").unbind(".contextToolbar");
                this.current.panel.remove();
                this.current = null;
            },
            /**
             * Returns true if the passed element is NOT the RTE anchor element for any current PropertyPanel, or
             * if the current PropertyPanel has changed in size.
             * @param el {Element} element to check against the current PropertyPanel anchor, if any.
             * @return {boolean}
             */
            hasAnchorChanged: function (el) {
                var c = this.current;
                if (c && $(c.anchor)[0] == $(el)[0]) {
                    return (c.options.originalHeight && (c.options.originalHeight != $(el).height()));
                }
                return true;
            },

            /**
             * Constructs a new AUI dialog containing a custom editor proxied from a remote app. Should be passed to the
             * MacroBrowser as a macro editor override. (See override.js for more details)
             *
             * @param {Object} macroData Data associated with the macro being edited.
             * @param {String} [macroData.name] the macro's name.
             * @param {String} [macroData.body] the body content of the macro (if any).
             * @param {Object} [macroData.params] stored key-value parameters associated with the macro.
             * @param {Object} opts Options to configure the behaviour and appearance of the editor dialog.
             * @param {String} [opts.url] url targeting a local proxy servlet for the remote application's custom macro editor.
             * @param {String} [opts.editTitle="Remotable Plugins Dialog Title"] dialog header to be used when editing an existing macro.
             * @param {String} [opts.insertTitle="Remotable Plugins Dialog Title"] dialog header to be used when inserting the macro for the first time.
             * @param {String|Number} [opts.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
             * @param {String|Number} [opts.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
             */
            openCustomPropertyPanel: function(macroData, opts, type, el, buttons, options) {
                // CE-74: if the editor loses focus before getBookmark() is called, a HierarchyRequestError
                // will occur in Internet Explorer, so restore focus just in case.
              //  alert("Custom property panel!");




                var panel = AJS("div").attr({"class": "panel-buttons"});
                for (var i = 0, ii = buttons.length; i < ii; i++) {
                    if (!buttons[i]) { continue; }

                    var button = buttons[i];
                    var html = button.html || '<span class="icon"></span>';
                    var classes = [];

                    if(button.text) {
                        html += '<span class="panel-button-text">' + button.text + '</span>';
                    }

                    button.className && classes.push(button.className);
                    button.disabled && classes.push("disabled");
                    button.selected && classes.push("selected");

                    !buttons[i + 1] && classes.push("last");
                    !buttons[i - 1] && classes.push("first");

                    var element;
                    if (!button.html) {
                        element = AJS("a").attr({
                            href: buttons[i].href || "#"
                        }).addClass('aui-button').html(html);
                        if (button.disabled) {
                            element.attr("title", button.disabledText);
                            element.disable();
                            element.click(function(e) {
                                return AJS.stopEvent(e);
                            });
                        } else {
                            buttons[i].click && (function(button, element, el) {
                                element.click(function(e) {
                                    button.click(element,el);
                                    return AJS.stopEvent(e);
                                });
                            })(buttons[i], element, el);
                        }
                    } else {
                        // If HTML has been provided use that instead of creating a button.
                        element = $(button.html);
                    }

                    button.tooltip && element.attr("data-tooltip", button.tooltip);
                    element.addClass(classes.join(" "));
                    panel.append(element);
                }
                return this.create(type, el, panel, options, macroData, opts);
                //AJS.Rte.getEditor().focus();
                //var editorSelection = AJS.Rte.getEditor().selection;
                //var bm = editorSelection.getBookmark();
                //
                //openEditorMacroData = macroData.params;
                //openEditorMacroBody = macroData.body;
                //
                //function getIframeHtmlForMacro(url) {
                //    var data = {
                //            "width": "100%",
                //            "height": "100%",
                //            "ui-params": _AP.uiParams.encode({dlg: 1})
                //        };
                //    $.extend(data, openEditorMacroData);
                //    return $.ajax(url, {
                //        data: data
                //    });
                //}
                //
                //saveMacro = function(updatedParameters, updatedMacroBody) {
                //    // Render the macro
                //    var macroRenderRequest = {
                //        contentId: Confluence.Editor.getContentId(),
                //        macro: {
                //            name: macroData.name,
                //            params: updatedParameters,
                //            // AC-741: MacroUtils clients in Confluence core set a non-existent macro body to the empty string.
                //            // In the absence of a public API, let's do the same to minimize the chance of breakage in the future.
                //            body: updatedMacroBody || (macroData.body ? macroData.body : "")
                //        }
                //    };
                //
                //    editorSelection.moveToBookmark(bm);
                //    tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
                //};
                //
                //var dialogOpts = {
                //    header: macroData.params ? opts.editTitle : opts.insertTitle,
                //    submitText: macroData.params ? "Save" : "Insert",
                //    chrome: true,
                //    ns: macroData.name,
                //    width: opts.width || null,
                //    height: opts.height || null
                //};
                //
                //dialog.create(dialogOpts, false);
                //
                //getIframeHtmlForMacro(opts.url).done(function(data){
                //    var dialogHtml = $(data);
                //    dialogHtml.addClass('ap-dialog-container');
                //    $('.ap-dialog-container').replaceWith(dialogHtml);
                //});

            }
        };

        return module;

    });


})(AJS.$, define);
