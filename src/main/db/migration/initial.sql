create table nota_negociacao
(
    id                   int auto_increment
        primary key,
    taxa_liquidacao      double null,
    emolumentos          double null,
    taxa_operacional     double null,
    impostos             double null,
    irpf_vendas          double null,
    `outros_custos-oper` double null,
    titulo               int    not null
)
    comment 'representa as notas de corretagem com as operacoes em renda variável';

create table operacao
(
    id                   int auto_increment
        primary key,
    nota_negociacao      int            null,
    tipo_operacao        char           not null comment 'compra ou venda',
    titulo               int            not null,
    qtde                 smallint       not null,
    valor_total_operacao decimal(12, 2) not null,
    custo_medio_venda    decimal(14, 4) null comment 'novo custo médio, após a operação',
    resultado_venda      decimal(12, 2) null,
    data                 date           not null
)
    comment 'representa uma operacao (compra/venda) realizada sobre um titulo ';

create table situacao_carteira
(
    id                int auto_increment
        primary key,
    data              date     not null,
    titulo            int      not null,
    qtde_disponivel   smallint not null,
    `valor-investido` double   not null,
    valor_atual       double   not null
)
    comment 'representa a situação da carteira em uma daterminada data';

create table titulo
(
    id                    int auto_increment
        primary key,
    ticker                varchar(7)                  not null,
    nome                  varchar(200)                null,
    tipo                  char                        not null,
    setor                 varchar(100)                null,
    qtde                  int            default 0    not null,
    valor_total_investido decimal(12, 2) default 0.00 not null,
    data_entrada          date                        not null,
    constraint titulo_ticker_uindex
        unique (ticker)
)
    comment 'Representa qualquer titulo (ações, tesouro direto, ouro, fundos etc)';

