insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where ticker = 'alup11'), 100, 1000.00,1000.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where ticker = 'aper3'), 200, 1000.00,1000.00);
insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from ativo where ticker = 'azul4'), 400, 1000.00,1000.00);
commit;
