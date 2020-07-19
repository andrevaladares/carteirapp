create table consolidacao_impostos_mes
(
	id int auto_increment,
	tipo_ativo varchar(5) not null,
	data date not null comment 'Data de referência da consolidação. Deve ser o último dia do mês de referência',
	resultado_mes decimal(12,2) not null comment 'Lucro ou prejuizo no mês para a classe de ativo',
	valor_total_vendas decimal(12,2) not null comment 'Valor total das vendas no mês',
	prejuizo_acumulado_mes_anterior decimal(12,2) not null comment 'Prejuizo acumulado até o mês anterior ao da consolidação. Pode ser usado para abatimento de imposto',
	prejuizo_abatimento decimal(12,2) not null comment 'Valor de prejuizo a utilizado para abater o resultado antes do cálculo do imposto devido',
	imposto_devido decimal(12,2) not null,
	data_pgto date null comment 'Data em que o imposto foi pago',
	valor_imposto_pago decimal(12,2) null comment 'Valor de imposto efetivamente pago, considerando multas e juros quando for o caso',
	constraint consolidacao_impostos_mes_pk
		primary key (id)
)
comment 'Guarda os valores consolidados relacionados a impostos para cada tipo de ativo para cada mês';

