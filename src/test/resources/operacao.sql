delete from ativo;
delete from operacao;

insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('visc11', 'vinci shopping fii', 'fii', 'shopping', 1, 10.12, '2020-02-28');

commit;