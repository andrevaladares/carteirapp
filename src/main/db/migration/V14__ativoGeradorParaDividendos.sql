alter table operacao
    add ativo_gerador int null comment 'Ativo gerador da operação, quando for o caso, como em dividendos, cujo ativo é uma moeda e o gerador uma ação';

alter table operacao modify tipo_operacao char(3) not null comment 'c. Compra
v. Venda
i. Imposto (come cotas)
ts. Transferencia de saida
te. Transferencia de entrada';
