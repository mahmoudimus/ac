A Confluence macro that loads the remote content as an IFrame.
Dynamic Content Macros render content on every page request and are suitable for add-ons that need to display content
that changes over time, or content that is specific to the authenticated user.

Json Example:
{
 "url": "/render-map",
 "description": {
   "value": "Shows a configurable map"
 },
 "icon": {
   "width": 80,
   "height": 80,
   "url": "/maps/icon.png"
 },
 "documentation": {
   "url": "http://docs.example.com/addons/maps"
 },
 "categories": [
   "visuals"
 ],
 "outputType": "block",
 "bodyType": "none",
 "aliases": [
   "map"
 ],
 "featured": true,
 "width": 200,
 "height": 200,
 "parameters": [
   {
     "identifier": "view",
     "name": {
       "value": "Map View"
     },
     "description": {
       "value": "Allows switching between view types"
     },
     "type": "enum",
     "required": true,
     "multiple": false,
     "defaultValue": "Map",
     "values": [
       "Map",
       "Satellite"
     ]
   }
 ],
 "name": {
   "value": "Maps"
 }
}