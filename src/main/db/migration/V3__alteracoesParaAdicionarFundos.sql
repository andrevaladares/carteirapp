alter table ativo modify qtde decimal(18,8) not null;
alter table operacao modify qtde decimal(18,8) not null;
alter table operacao modify custo_medio_venda decimal(18,8) null comment 'novo custo médio, após a operação';
