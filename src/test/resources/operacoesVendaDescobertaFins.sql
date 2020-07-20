insert into consolidacao_impostos_mes (tipo_ativo, data, resultado_mes, valor_total_vendas, prejuizo_acumulado_mes, base_calculo_imposto, imposto_devido)
values ('fin', '2019-12-31', 0, 0, 0.00,0.00, 0.00);

insert into ativo (id, ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo, valor_investido_dolares)
VALUES (1, 'bova11', 'etf ibov', 'fin', 'etfs', 0, 0.00, '2019-11-01', null, 0.00);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (1, null, 'v', 1, 100, 10000.00, null, null, '2020-01-12', null, 0.00, 0.00, 0.00);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (2, null, 'v', 1, 10, 1000.00, null, null, '2020-01-15', null, 0.00, 0.00, 0.00);

commit;