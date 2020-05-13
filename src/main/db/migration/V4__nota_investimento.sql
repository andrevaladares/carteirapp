create table nota_investimento
(
    id int auto_increment,
    data_movimentacao date not null,
    cnpj_corretora varchar(14) not null,
    nome_corretora varchar(100) null,
    constraint nota_investimento_pk
        primary key (id)
);

alter table operacao
    add nota_investimento int null;

alter table operacao
    change custo_medio_venda custo_medio_operacao decimal(18,8) null
        comment '';

alter table operacao modify custo_medio_operacao decimal(18,8) null comment 'Se operação de compra, registra o custo médio da operação; se operação de venda, registra o custo médio do ativo no momento da operação (utilizado para calcular o resultado)';

