insert into ativo (id, ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo, valor_investido_dolares)
VALUES (1, 'stne', 'Stone', 'aus', 'financeira', 200, 35000.00, '2019-12-01', null, 7000.00);

insert into operacao (id, nota_negociacao, tipo_operacao, ativo, qtde, valor_total_operacao, custo_medio_operacao, resultado_venda, data,
                      nota_investimento, valor_total_dolares, custo_medio_dolares, resultado_venda_dolares)
values (1, null, 'c', 1, 100, 15000.00, null, null, '2020-01-10', null, 3000.00, 0.00, 0.00);

commit;