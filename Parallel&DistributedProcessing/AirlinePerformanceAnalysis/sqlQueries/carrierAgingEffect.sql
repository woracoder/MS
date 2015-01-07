use Airlines 

--select TailNum,min(DATEFROMPARTS( Year,Month,DayofMonth)),max(DATEFROMPARTS( Year,Month,DayofMonth)),DATEDIFF(dd,min(DATEFROMPARTS( Year,Month,DayofMonth)),max(DATEFROMPARTS( Year,Month,DayofMonth)))  from allyears 
--group by TailNum 


select TailNum,year
,avg( CONVERT(int, 
 case when isnumeric(WeatherDelay) = 1  then WeatherDelay
	  else 0 end 
) ) WeatherDelay
,avg( CONVERT(int, 
 case when isnumeric( NASDelay ) = 1  then NASDelay
	  else 0 end 
) ) NASDelay
,avg( CONVERT(int, 
 case when isnumeric( SecurityDelay ) = 1  then SecurityDelay
	  else 0 end 
) ) SecurityDelay
,avg( CONVERT(int, 
 case when isnumeric( LateAircraftDelay ) = 1  then LateAircraftDelay
	  else 0 end 
) ) LateAircraftDelay
,avg( CONVERT(int, 
 case when isnumeric(CarrierDelay) = 1  then CarrierDelay
	  else null end 
) ) CarrierDelay
--min(DATEFROMPARTS( Year,Month,DayofMonth)),max(DATEFROMPARTS( Year,Month,DayofMonth)),DATEDIFF(dd,min(DATEFROMPARTS( Year,Month,DayofMonth)),max(DATEFROMPARTS( Year,Month,DayofMonth)))  
from allyears 
group by TailNum,Year 
order by TailNum,Year asc 
