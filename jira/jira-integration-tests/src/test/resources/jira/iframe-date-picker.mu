<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
</head>
<body>
    <form>
        <div id="date-time-container">
            <label>Date time</label>
            <input type="text" id="date-time-field" value="" />
            <span id="date-time-trigger">&#128197;</span>
        </div>
        <script type="text/javascript">
            AP.require('jira', function(jira){
                var dateField = document.querySelector("#date-time-field");
                var dateTrigger = document.querySelector("#date-time-trigger");

                dateTrigger.addEventListener("click", function() {
                    jira.datePicker({
                        element: dateTrigger,
                        date: "2016/1/2 03:56 PM",
                        showsTime: true,
                        onSelect: function (date, isoDate) {
                            dateField.value = date;
                            dateField.setAttribute("data-iso", isoDate);
                        }
                    });
                });
            });
        </script>

        <div id="date-container">
            <label>Date</label>
            <input type="text" id="date-field" value="" />
            <span id="date-trigger">&#128197;</span>
        </div>
        <script type="text/javascript">
            AP.require('jira', function(jira){
                var dateField = document.querySelector("#date-field");
                var dateTrigger = document.querySelector("#date-trigger");

                dateTrigger.addEventListener("click", function() {
                    jira.datePicker({
                        element: dateTrigger,
                        date: "2016/1/2",
                        showsTime: false,
                        onSelect: function (date, isoDate) {
                            dateField.value = date;
                            dateField.setAttribute("data-iso", isoDate);
                        }
                    });
                });
            });
        </script>

        <div id="today-container">
            <label>Date</label>
            <input type="text" id="today-field" value="" />
            <span id="today-trigger">&#128197;</span>
        </div>
        <script type="text/javascript">
            AP.require('jira', function(jira){
                var dateField = document.querySelector("#today-field");
                var dateTrigger = document.querySelector("#today-trigger");

                dateTrigger.addEventListener("click", function() {
                    jira.datePicker({
                        element: dateTrigger,
                        showsTime: true,
                        onSelect: function (date, isoDate) {
                            dateField.value = date;
                            dateField.setAttribute("data-iso", isoDate);
                        }
                    });
                });
            });
        </script>
    </form>
</body>
</html>
