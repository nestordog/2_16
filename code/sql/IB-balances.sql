SET @base_currency = 'EUR';

select
    u.STRATEGY_FK,
    u.CURRENCY,
    sum(u.AMOUNT)
from (
    select
    t1.id,
    t1.STRATEGY_FK as STRATEGY_FK,
    t1.CURRENCY as CURRENCY,
    case t1.TYPE
         when 'CREDIT' then t1.PRICE
         when 'INTREST_RECEIVED' then t1.PRICE
         when 'DEBIT' then -t1.PRICE
         when 'INTREST_PAID' then -t1.PRICE
         when 'FEES' then -t1.PRICE
         when 'REFUND' then t1.PRICE
         else -t1.QUANTITY * t1.PRICE * sf1.CONTRACT_SIZE
    end as AMOUNT
    from transaction t1
    left join security s1 on t1.SECURITY_FK = s1.id
    left join security_family sf1 on s1.SECURITY_FAMILY_FK = sf1.id
union
     select
     t2.id,
     t2.STRATEGY_FK as STRATEGY_FK,
     case when f2.id is null then t2.CURRENCY else @base_currency end as CURRENCY,
     -t2.COMMISSION as AMOUNT
     from transaction t2
     left join security s2 on t2.SECURITY_FK = s2.id
     left join forex f2 on s2.SECURITY_FAMILY_FK = f2.id
) as u
group by u.STRATEGY_FK, u.CURRENCY

