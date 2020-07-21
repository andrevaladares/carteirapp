insert into consolidacao_impostos_mes (tipo_ativo, data, resultado_mes, valor_total_vendas, prejuizo_acumulado_mes, base_calculo_imposto, imposto_devido)
values ('aus', '2019-12-31', 0, 0, 0, 0.00, 0.00);

insert into ativo (id, ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo, valor_investido_dolares)
VALUES (1, 'stne', 'stone', 'aus', 'financeira', 200, 35000.00, '2019-12-01', null, 7000.00);

insert into ativo (id, ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo, valor_investido_dolares)
VALUES (2, 'sq', 'square', 'aus', 'financeira', 100, 12000.00, '2019-11-01', null, 2500.00);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (1, null, 'c', 1, 100, 15000.00, null, null, '2020-01-10', null, 3000.00, 0.00, 0.00);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (2, null, 'v', 1, 100, 20000.00, 166.67, 1333.33, '2020-01-12', null, 4000.00, 33.33, 266.67);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (3, null, 'v', 2, 100, 15001.00, 120.00, 300.00, '2020-01-15', null, 2999.80, 24.00, 60.00);

commit;