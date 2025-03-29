# Projeto Laboratório de Base de Dados

Aplicação simples em Java que se conecta ao SGBD Apache Cassandra e executa as seguintes operações:
- Conexão com o banco de dados.
- Inserção de dados.
- Consulta aos dados inseridos.
- Atualização de dados existentes.
- Exclusão de dados.

## Pre-requisitos
- Conexão com o banco de dados Cassandra
## Rodando localmente

Clone o projeto

```bash
  git clone https://github.com/ericarfs/ProjetoBDJava
```

Entre no diretório do projeto

```bash
  cd ProjetoBD
```

Instale as dependências

```bash
  mvn clean install
```

Compile o projeto

```bash
  mvn clean package
```

Inicie o projeto

```bash
   java -jar target/ProjetoBD-1.0-SNAPSHOT.jar
```

