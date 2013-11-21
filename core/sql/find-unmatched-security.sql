select * from security where id not in (
    select id from `option`
    union select id from future
    union select id from forex
    union select id from stock
    union select id from fund
    union select id from `index`
    union select id from generic_future
    union select id from implied_volatility
    union select id from intrest_rate
    union select id from bond
    union select id from commodity
    union select id from combination
)
