insert into situacao_carteira (data, titulo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from titulo where ticker = 'alup11'), 100, 1000.00,1000.00);
insert into situacao_carteira (data, titulo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from titulo where ticker = 'aper3'), 200, 1000.00,1000.00);
insert into situacao_carteira (data, titulo, qtde_disponivel, valor_investido, valor_atual)
VALUES ('2020-02-28', (select id from titulo where ticker = 'azul4'), 400, 1000.00,1000.00);
commit;
