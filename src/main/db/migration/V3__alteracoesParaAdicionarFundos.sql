alter table ativo modify qtde decimal(18,8) not null;
alter table operacao modify qtde decimal(18,8) not null;
alter table operacao modify custo_medio_venda decimal(18,8) null comment 'novo custo médio, após a operação';
alter table ativo modify tipo char(3) not null;
update ativo set tipo='fii' where tipo = 'f';
update ativo set tipo='fin' where tipo = 'i';
update ativo set tipo='oz2' where tipo = 'o';
alter table ativo
    add cnpj_fundo varchar(14) null;
alter table ativo modify ticker varchar(7) null;