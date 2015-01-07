use Airlines

select 
year
,month
,sum(CONVERT(int, 
 case when isnumeric(WeatherDelay) = 1  then WeatherDelay
	  else 0 end 
) ) WeatherDelay
,sum( CONVERT(int, 
 case when isnumeric( NASDelay ) = 1  then NASDelay
	  else 0 end 
) ) NASDelay
,sum( CONVERT(int, 
 case when isnumeric( SecurityDelay ) = 1  then SecurityDelay
	  else 0 end 
) ) SecurityDelay
,sum( CONVERT(int, 
 case when isnumeric( LateAircraftDelay ) = 1  then LateAircraftDelay
	  else 0 end 
) ) LateAircraftDelay
,sum( CONVERT(int, 
 case when isnumeric(CarrierDelay) = 1  then CarrierDelay
	  else 0 end 
) ) CarrierDelay
, sum( 
 case when 
 CONVERT(int, 
 case when isnumeric(ArrDelay) = 1  then ArrDelay
	  else 0 end 
) > 0 then 
CONVERT(int, 
 case when isnumeric(ArrDelay) = 1  then ArrDelay
	  else 0 end 
)
else 0 end)  actArrDelay,
count( 
 case when 
 CONVERT(int, 
 case when isnumeric(ArrDelay) = 1  then ArrDelay
	  else 0 end 
) > 0 then 
CONVERT(int, 
 case when isnumeric(ArrDelay) = 1  then ArrDelay
	  else 0 end 
)
else NULL end)  actArrDelay_count
,count(1) flightCount 
/*,count(
	  case when isnumeric(WeatherDelay) = 1  then 1
	  else NULL end 
	  ) count_airlineConsideredDelay*/
,
count(
case when
(CONVERT(int, 
 case when isnumeric(WeatherDelay) = 1  then WeatherDelay
	  else 0 end 
) + 
CONVERT(int, 
 case when isnumeric( NASDelay ) = 1  then NASDelay
	  else 0 end 
) 
+ CONVERT(int, 
 case when isnumeric( SecurityDelay ) = 1  then SecurityDelay
	  else 0 end 
) +
 CONVERT(int, 
 case when isnumeric( LateAircraftDelay ) = 1  then LateAircraftDelay
	  else 0 end 
) + CONVERT(int, 
 case when isnumeric(CarrierDelay) = 1  then CarrierDelay
	  else 0 end 
) ) > 0 then 1 else null end 
) count_airlineConsideredDelay 
from allyears
group by year
,month
order by year,month


