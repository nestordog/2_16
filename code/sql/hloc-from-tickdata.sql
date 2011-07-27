select
date(FROM_UNIXTIME(datetime / 1000)) as date,
max(current) as high,
min(current) as low,
CAST(SUBSTRING_INDEX(GROUP_CONCAT(CAST(current AS CHAR) ORDER BY id),',',1) AS DECIMAL(9,2)) as first,
CAST(SUBSTRING_INDEX(GROUP_CONCAT(CAST(current AS CHAR) ORDER BY id desc),',',1) AS DECIMAL(9,2)) as last,
count(current) as count
from smi
group by date
having count > 400

