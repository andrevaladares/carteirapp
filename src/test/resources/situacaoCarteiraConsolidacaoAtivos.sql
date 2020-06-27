insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where cnpj_fundo = '3319016000150' and data_entrada = '2020-03-18'), 3486.1505, 20938.07,21500.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where cnpj_fundo = '3319016000150' and data_entrada = '2020-04-18'), 4000.0000, 30000.00,29500.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where cnpj_fundo = '3319016000150' and data_entrada = '2020-06-15'), 2000.0000, 19000.00,19230.00);

insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where nome = 'Tesouro Selic 2025' and data_entrada='2020-03-18'), 6.63000000, 69938.07,70200.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where nome = 'Tesouro Selic 2025' and data_entrada='2020-03-16'), 0.86000000, 9068.96,10000.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where nome = 'Tesouro Selic 2025' and data_entrada='2020-03-24'), 4.64000000, 48974.17,50000.00);

commit;
