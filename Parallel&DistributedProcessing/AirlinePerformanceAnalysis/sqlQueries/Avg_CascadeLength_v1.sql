use Airlines

;with CTE as 
(
select 
TailNum
,year
,month
,DayofMonth
,Origin
,Dest
,WeatherDelay
,NASDelay
,SecurityDelay
,LateAircraftDelay
,CarrierDelay
,case when isnumeric(CRSArrTime) = 1 then cast(CRSArrTime as int) else 2500 end CRSArrTime
,case when isnumeric(CRSDepTime) = 1 then cast(CRSDepTime as int) else 2500 end CRSDept
,ArrTime
,DepTime
,IsDepDelayed
,IsArrDelayed
,row_number() over (partition by TailNum order by TailNum,Year,Month,DayofMonth,case when isnumeric(CRSArrTime) = 1 then cast(CRSArrTime as int) else 2500 end asc) rowNum
from 
allyears 
where year ='2008' and rtrim(ltrim(TailNum)) <>''
--order by TailNum,Year,Month,DayofMonth,case when isnumeric(CRSArrTime) = 1 then cast(CRSArrTime as int) else 2500 end
) ,
cascadeResults as (
select A.*/*A.* into cascadeResults*/ from CTE A inner join CTE B 
on 
A.TailNum = B.TailNum and 
A.rowNum+1 = B.rowNum and A.Dest = B.Origin 
and A.IsDepDelayed	= 'YES'
--order by TailNum,rowNum
)
/*with CTE2 as 
(
select *,row_number() over(partition by TailNum order by TailNum,rowNum) rowId from cascadeResults 
)
select A.TailNum,A.rowNum,B.rowNum,A.* from CTE2 A left join CTE2 B on A.rowNum = B.rowNum+1 and A.TailNum = B.TailNum
order by A.TailNum,A.rowNum
*/
,CTE3 as 
(
	select A.TailNum,A.rowNum,A.rowNum start  from  cascadeResults A 
	left join cascadeResults B on A.TailNum = B.TailNum and A.rowNum = B.rowNum +1
	where B.TailNum is null 
	
	union All 
	
	select B.TailNum,B.rowNum,CTE3.start from CTE3 inner join cascadeResults B 
	on CTE3.rowNum + 1=  B.rowNum and CTE3.TailNum = B.TailNum 
),
CTE4 as 
(
select TailNum,start,count(1) cascadeLen from CTE3 
group by tailnum,start
)
select sum(cascadeLen),COUNT(1)
from CTE4 
where cascadeLen > 1 
---order by tailnum,start


