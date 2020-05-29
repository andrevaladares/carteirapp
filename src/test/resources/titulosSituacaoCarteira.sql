delete from ativo;
insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('alup11', 'Alupar', 'a', 'nao_sei', 400, 1000.00, '2020-01-01');

insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('visc11', 'Vinci Shoppings Fii', 'fii', 'nao_sei', 80, 2500.00, '2020-01-01');

insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('smal11', 'Smalcaps', 'fin', 'Fundos de indice', 170, 800.00, '2020-01-01');

insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('oz2', 'Ouro a vista fracionario', 'oz2', 'ouro', 200, 1500.00, '2020-01-01');

insert into ativo (nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo)
values ('VOTORANTIM FIC DE FI CAMBIAL DÓLAR', 'fiv', 'Fundo cambial', 130, 3000.00, '2020-01-01', '05217065000107');

insert into ativo (nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('Tesouro Prefixado com Juros Semestrais 2029', 'tis', 'Tesouro direto', 100, 2000.00, '2020-01-01');

insert into ativo (nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('DEB LIGHT SERVICOS DE ELETRIC - OUT/2022', 'deb', 'Credito privado', 100, 2000.00, '2020-01-01');

insert into ativo (nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('CRI Direcional - ABR/2021', 'cri', 'Credito imobiliario', 100, 2000.00, '2020-01-01');

insert into ativo (nome, tipo, setor, qtde, valor_total_investido, data_entrada)
values ('CDB BANCO BMG', 'cdb', 'Credito privado', 100, 2000.00, '2020-01-01');

commit;