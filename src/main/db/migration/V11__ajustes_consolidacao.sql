alter table consolidacao_impostos_mes change prejuizo_acumulado_mes_anterior prejuizo_acumulado_mes decimal(12,2) not null comment 'Prejuizo acumulado, resultante da consolidação.
igual a prejuizoAcumuladoNoMesAnterior - resultadoAtual (se tiver sido prjuizo aumentará)';

alter table consolidacao_impostos_mes change prejuizo_abatimento base_calculo_imposto decimal(12,2) not null comment 'Se resultado no mês for positivo:
resultado - prejuizo mes anterior (até zerar apenas)
';

