drop table if exists mensagens;
create table mensagens (
  id integer primary key autoincrement,
  nome text not null,
  mensagem text not null
);
