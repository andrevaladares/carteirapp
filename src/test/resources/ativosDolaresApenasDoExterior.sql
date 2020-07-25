insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, valor_investido_dolares, data_entrada)
values ('br$', 'dolar obtido via remessa do brasil', 'm', 'cambio', 0, 0, 0.00, '2020-02-28');

insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, valor_investido_dolares, data_entrada)
values ('us$', 'dolar obtido via operações no exterior', 'm', 'cambio', 2000, 10500, 0.00, '2020-02-28');

commit;