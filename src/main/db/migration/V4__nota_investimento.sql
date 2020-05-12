create table nota_investimento
(
    id int auto_increment,
    data_movimentacao date not null,
    cnpj_corretora varchar(14) not null,
    nome_corretora varchar(100) null,
    constraint nota_investimento_pk
        primary key (id)
);


