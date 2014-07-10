(: XQuery Join Query :)
(: Find the highest recorded temperature (TMAX) for each station for each     :)
(: day over the year 2000.                                                    :)
let $sensor_collection := "../../../../../../../weather_data/dataset-tiny-local/data_links/local_speed_up/d0_p1_i0/sensors/?select=*.xml;recurse=yes"
for $r in collection($sensor_collection)/root/dataCollection/data

let $station_collection := "../../../../../../../weather_data/dataset-tiny-local/data_links/local_speed_up/d0_p1_i0/stations/?select=*.xml;recurse=yes"
for $s in collection($station_collection)/root/stationCollection/station

where $s/id eq $r/station
    and $r/dataType eq "TMAX" 
    and fn:year-from-dateTime(xs:dateTime(fn:data($r/date))) eq 2000
return ($s/displayName, $r/date, $r/value)