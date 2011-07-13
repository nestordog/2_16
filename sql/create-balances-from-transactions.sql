select
      t2.STRATEGY_FK,
      t2.CURRENCY,
      sum(
            case t2.TYPE
            when 'CREDIT' then t2.PRICE
            when 'INTREST_RECEIVED' then t2.PRICE
            when 'DEBIT' then -t2.PRICE
            when 'INTREST_PAID' then -t2.PRICE
            when 'FEES' then -t2.PRICE
            else -t2.QUANTITY * t2.PRICE * f2.CONTRACT_SIZE - t2.COMMISSION
            end
      ) as amount
from transaction t2
left join security s2 on t2.SECURITY_FK = s2.id
left join security_family f2 on s2.SECURITY_FAMILY_FK = f2.id
group by t2.STRATEGY_FK, t2.CURRENCY
