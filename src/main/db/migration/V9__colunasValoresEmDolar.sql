alter table operacao
	add valor_total_dolares decimal(12,2) null;

alter table operacao
	add custo_medio_dolares decimal(18,8) null;

alter table operacao
	add resultado_venda_dolares decimal(12,2) null;

alter table ativo
    add valor_investido_dolares decimal(12,2) null;

alter table nota_negociacao
    add valor_dolar_na_data decimal(12,2) null;

